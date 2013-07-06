// ChatServer
//package cs149.chat;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.LinkedList;


public class ChatServer {
    private static final Charset utf8 = Charset.forName("UTF-8");

    private static final String OK = "200 OK";
    private static final String NOT_FOUND = "404 NOT FOUND";
    private static final String HTML = "text/html";
    private static final String TEXT = "text/plain";

    private static final Pattern PAGE_REQUEST
            = Pattern.compile("GET /([^ /]+)/chat\\.html HTTP.*");
    private static final Pattern PULL_REQUEST
            = Pattern.compile("POST /([^ /]+)/pull\\?last=([0-9]+) HTTP.*");
    private static final Pattern PUSH_REQUEST
            = Pattern.compile("POST /([^ /]+)/push\\?msg=([^ ]*) HTTP.*");

    private static final String CHAT_HTML;
    private static final int TotalThreads = 8; 
    private Worker2[] workers;
//    private final Semaphore semaphore;
    private LinkedList<Socket> connections;
    private Object connection_lock;
//    private ChatState all;
    
    static {
        try {
            CHAT_HTML = getResourceAsString("chat.html");
        } catch (final IOException xx) {
            throw new Error("unable to start server", xx);
        }
    }

    private final int port;
    private final Map<String,ChatState> stateByName = new HashMap<String,ChatState>();

    /** Constructs a new <code>ChatServer</code> that will service requests on
     *  the specified <code>port</code>.  <code>state</code> will be used to
     *  hold the current state of the chat.
     */
    public ChatServer(final int port) throws IOException {
        this.port = port;
        
    	workers = new Worker2[TotalThreads];
		connections = new LinkedList<Socket>();
		connection_lock = new Object();
//		all = new ChatState("all");
    }

    public void runForever() throws Exception {
        final ServerSocket server = new ServerSocket(port);
        
        
     //   (new WorkerManagement()).start();
		for(int j=0;j<TotalThreads;j++){
			workers[j] = new Worker2();
			workers[j].start();
		}
        
        while (true) {
        	final Socket connection = server.accept();
            synchronized (connection_lock) {
                if(connection != null) connections.addLast(connection);
                connection_lock.notify();				
			}            
        }
    }

    private void handle(final Socket connection) {
        try {
            final BufferedReader xi
                    = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            final OutputStream xo = connection.getOutputStream();
            
            final String request = xi.readLine();
            
            System.out.println(Thread.currentThread() + ": " + request);

            Matcher m;
            if (PAGE_REQUEST.matcher(request).matches()) {
                sendResponse(xo, OK, HTML, CHAT_HTML);
            }
            else if ((m = PULL_REQUEST.matcher(request)).matches()) {
                final String room = m.group(1);
                final long last = Long.valueOf(m.group(2));
                sendResponse(xo, OK, TEXT, getState(room).recentMessages(last));
            }
            else if ((m = PUSH_REQUEST.matcher(request)).matches()) {
                final String room = m.group(1);
                final String msg = m.group(2);
                    getState(room).addMessage(msg);                	
                sendResponse(xo, OK, TEXT, "ack");
            }
            else {
                sendResponse(xo, NOT_FOUND, TEXT, "Nobody here with that name.");
            }

            connection.close();
        }
        catch (final Exception xx) {
            xx.printStackTrace();
            try {
                connection.close();
            }
            catch (final Exception yy) {
                // silently discard any exceptions here
            }
        }
    }

    /** Writes a minimal-but-valid HTML response to <code>output</code>. */
    private static void sendResponse(final OutputStream output,
                                     final String status,
                                     final String contentType,
                                     final String content) throws IOException {
    	System.out.println(" Printing " + content + " ,status " + status + "\n");
        final byte[] data = content.getBytes(utf8);
        final String headers =
                "HTTP/1.0 " + status + "\n" +
                "Content-Type: " + contentType + "; charset=utf-8\n" +
                "Content-Length: " + data.length + "\n\n";

        final BufferedOutputStream xo = new BufferedOutputStream(output);
        xo.write(headers.getBytes(utf8));
        xo.write(data);
        xo.flush();

        System.out.println(Thread.currentThread() + ": replied with " + data.length + " bytes");
    }

    private ChatState getState(final String room) {
    	if(room.equals("all")) return ChatState.all_pointer;
        ChatState state = stateByName.get(room);
        if (state == null) {
            state = new ChatState(room);
            stateByName.put(room, state);
        }
        return state;
    }

    /** Reads the resource with the specified name as a string, and then
     *  returns the string.  Resource files are searched for using the same
     *  classpath mechanism used for .class files, so they can either be in the
     *  same directory as bare .class files or included in the .jar file.
     */
    private static String getResourceAsString(final String name) throws IOException {
        final Reader xi = new InputStreamReader(
                ChatServer.class.getClassLoader().getResourceAsStream(name));
        try {
            final StringBuffer result = new StringBuffer();
            final char[] buf = new char[8192];
            int n;
            while ((n = xi.read(buf)) > 0) {
                result.append(buf, 0, n);
            }
            return result.toString();
        } finally {
            try {
                xi.close();
            } catch (final IOException xx) {
                // discard
            }
        }
    }

    /** Runs a chat server, with a default port of 8080. */
    public static void main(final String[] args) throws Exception {
        final int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        new ChatServer(port).runForever();
    }

    private class Worker2 extends Thread {
    	Socket conn;
    	boolean valid=false;
    	public void run() {
    		while(true){
    			synchronized (connection_lock) {
      				try {
   						connection_lock.wait(10000);  // limited to 10s to recover from missed notifies, although I am not expecting any missed notifies. 
   					} catch (InterruptedException e) {
   						// TODO Auto-generated catch block
   						e.printStackTrace();
   					}    					
    			}
   				if(!connections.isEmpty()){
   	    			synchronized (connection_lock) {
   	    				if(!connections.isEmpty()) {
   	    					conn = connections.pop();
   	    					valid = true;
   	    				}
   	    			}
   	    			if (valid == true) handle(conn);
   	    			valid = false;
				}
    		}
    	}
    }

    
//    private class Worker extends Thread {
//    	Socket Wconnection;
//    	private Worker(Socket Connection) {
//    			Wconnection = Connection;
//    	}
//    	public void run() {
//    		handle(Wconnection);
//    		System.out.println("Available Semaphores \n" + semaphore.availablePermits()); // remove comment
//    		semaphore.release();
////    		System.out.println("Releasing thread \n");
//    	}
//    }
//    
//    private class WorkerManagement extends Thread {
//    	public void run(){
//    		while(true){
//    			synchronized (connection_lock) {
//        			while(!connections.isEmpty()){
//        				if(semaphore.tryAcquire()){
//        					(new Worker(connections.pop())).start();
//        				}
//                    }
//					try {
//						connection_lock.wait();
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}    			
//    		}
//    	}
//    }
}
