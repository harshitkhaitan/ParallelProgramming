// ChatState
//package cs149.chat;

import java.util.LinkedList;


public class ChatState {
    private static final int MAX_HISTORY = 32;

    private final String name;
    private final LinkedList<ChatMessage> history = new LinkedList<ChatMessage>();
    private static long lastID = 0;   // Shared by all rooms
    private static IdRoomMap idMap = new IdRoomMap();
    private static Object addlock = new Object();  // shared by all rooms
    private Object waitlock;
    private static Object allwaitlock = new Object();
    public static ChatState all_pointer=new ChatState("all");
    public static LinkedList<ChatState> roomList = new LinkedList<ChatState>();

    public ChatState(final String name) {
        this.name = name;
        waitlock = new Object();
        if(!name.equals("all")) roomList.add(this);
        synchronized (addlock){
            lastID++;
            history.addLast(new ChatMessage(lastID, "Hello " + name + "!", name));        	
        	idMap.AddId(lastID, this);
        }
     }

    public String getName() {
        return name;
    }

    public Object getAddLock() {
        return addlock;
    }

    public Object getWaitLock(){
    	return waitlock;
    }
       
    private synchronized LinkedList<ChatMessage> getHistory() {
		return history;
	}

	public void addMessage(final String msg) {

    	synchronized (addlock) {
        	lastID = lastID + 1;
        	System.out.println("Received msg: " + msg);
        	if(history.size() < MAX_HISTORY){
        		history.addLast(new ChatMessage(lastID, msg, name));
        	}else{
        		history.removeFirst();
        		history.addLast(new ChatMessage(lastID, msg, name));
        	}
        	idMap.AddId(lastID, this);
		}
    	synchronized(waitlock){
        	waitlock.notifyAll();
    	}
    	
    	synchronized(allwaitlock){
        	allwaitlock.notifyAll();
    	}

    	if(this.getName().equals("all")){
    		for (ChatState room: roomList){
    			synchronized(room.getWaitLock()){
    				room.getWaitLock().notifyAll();
    			}
    		}
    	}
    	
    }

    public  String recentMessages(long mostRecentSeenID){
    	ChatMessage cm;
    	
    	if(this.name.equals("all")){
    		return recentMessagesAll(mostRecentSeenID);
       	}
    	
    	synchronized (addlock) {
    		cm = FetchMsg(mostRecentSeenID);
    		if (cm!=null) return cm.toString();
		}    	

    	try {
    			synchronized (waitlock){
    				waitlock.wait(15000);
    			}
    	} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
    	
    	synchronized (addlock) {
    		cm = FetchMsg(mostRecentSeenID);
    		if (cm!=null) return cm.toString();
		}    	

    	return Thread.currentThread().toString(); //FIXME
    	
    }
   
    public  String recentMessagesAll(long mostRecentSeenID){
    	ChatMessage cm;
    	    	
    	synchronized (addlock) {
    		cm = FetchMsgAll(mostRecentSeenID);
    		if (cm!=null) return cm.toString();
		}    	

    	try {
    			synchronized (allwaitlock){
    				allwaitlock.wait(15000);
    			}
    	} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
    	
    	synchronized (addlock) {
    		cm = FetchMsgAll(mostRecentSeenID);
    		if (cm!=null) return cm.toString();
		}    	

    	return Thread.currentThread().toString(); //FIXME
    	
    }
   
    
    private ChatMessage FetchMsg (long recentSeenID){
    	ChatMessage cm;
    	if(recentSeenID < lastID){
    		for (int i=0; i<MAX_HISTORY; i++){
    			if(i<history.size()){
    				cm = history.get(i);       		
    				if(cm.getID() > (recentSeenID)){
    					System.out.println("recent " + recentSeenID + " , last " + lastID);
    					System.out.println("Sent msg " + cm.toString());
    					return cm;
    				}
        		}
        		if(i<all_pointer.getHistory().size()){
        			cm = all_pointer.getHistory().get(i);       		
        			if(cm.getID() > (recentSeenID)){
        				System.out.println("recent " + recentSeenID + " , last " + lastID);
        					System.out.println("Sent msg " + cm.toString());
        			return cm;
        			}
            	}
        	}
    	}
    	return null;   	
    }
    
    private ChatMessage FetchMsgAll (long recentSeenID){
    	if(recentSeenID < lastID){
    		for(long i=recentSeenID+1;i<=lastID;i++){
        			ChatState cs = idMap.GetRoomForID(i);    				
        			for(int j=0;j<cs.getHistory().size();j++){
        				ChatMessage cm = cs.getHistory().get(j);
        				if (cm.getID() == i) return cm;
        			}  				
    		}
    	}
    	return null;
    }
    
    private class ChatMessage{
    	private long ID;
    	private String Msg;

    	public ChatMessage(long iD, String msg, String RoomName) {
    		ID = iD;
    		Msg = msg;
    	}

    	private synchronized long getID() {
			return ID;
		}

		private synchronized String getMsg() {
			return Msg;
		}

		private synchronized void setID(long iD) {
			ID = iD;
		}

		private synchronized void setMsg(String msg) {
			Msg = msg;
		}

		@Override
    	public String toString() {
    		if(ID == 0){
    	//		return Msg;
    		}
    		return ID + ": " + Msg + "\n";
    	//	return Msg;
    	}   	
    }
    
    private static class IdRoomMap{
    	private static LinkedList<IdRoomPair> pairlist = new LinkedList<IdRoomPair>();
    	   
		public void AddId (long ID, ChatState Name){
			pairlist.add(new IdRoomPair(ID, Name));
		}
    
		public ChatState GetRoomForID (long ID){
			for (IdRoomPair pair: pairlist){
				if (ID == pair.getID()){
					return pair.getRoomName();
				}
			}
			return null;
		}
		
		private class IdRoomPair{
    		final long ID;
    		final ChatState RoomName;
			public IdRoomPair(long iD, ChatState roomName) {
				ID = iD;
				RoomName = roomName;
			}
			private synchronized long getID() {
				return ID;
			}
			private synchronized ChatState getRoomName() {
				return RoomName;
			}
    	}   	
    }
}
