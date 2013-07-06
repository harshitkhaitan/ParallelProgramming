//package cs149.stm;

import java.util.concurrent.atomic.AtomicLong;

import org.deuce.Atomic;

public class STMTreap implements IntSet {
    static class Node {
        final int key;
        final int priority;
        Node left;
        Node right;

        Node(final int key, final int priority) {
            this.key = key;
            this.priority = priority;
        }

        public String toString() {
            return "Node[key=" + key + ", prio=" + priority +
                    ", left=" + (left == null ? "null" : String.valueOf(left.key)) +
                    ", right=" + (right == null ? "null" : String.valueOf(right.key)) + "]";
        }
    }

//    private class NodeState {
//    	Node cNode;
//    	Node local;
//    	boolean cSuccess;
//		public NodeState(Node nodeIn, boolean stateIn) {
//			cNode = nodeIn;
//			cSuccess = stateIn;
//			local = new Node(nodeIn.key, nodeIn.priority);
//			if(cNode.right != null) local.right = cNode.right;
//			if(cNode.left != null) local.left = cNode.left;
//		}
//		public boolean isChanged(){
//			if(cNode == null) {
//				if(local == null) return false;
//				return true;
//			}
//			if(local.key != cNode.key) return true;
//			if(local.priority != cNode.priority) return true;
//			if(local.left != null) {
//				if(cNode.left == null) return true;				
//				if(local.left != cNode.left) return true;
//			}else{
//				if(cNode.left != null) return true;
//			}
//			if(local.right != null) {
//				if(cNode.right == null) return true;				
//				if(local.right != cNode.right) return true;
//			}else {
//				if(cNode.right != null) return true;
//			}
//			return false;
//		}
//    }
    
    private AtomicLong randState = new AtomicLong(0);
    private Node root;

    @Atomic
    public boolean contains(final int key) {
        Node node = root;
        while (node != null) {
            if (key == node.key) {
                return true;
            }
            node = key < node.key ? node.left : node.right;
        }
        return false;
    }

    
    public void add(final int key) {
//    	if(root==null) root = new Node(key, randPriority());
//        addImpl(root, key);
//    	if(randState==null) randState = new AtomicLong(0);
    	if(contains(key)){
    		return;
    	}
    	long newRandom, originalRandom;
    	originalRandom = randState.get();
    	newRandom = randPriority(originalRandom);
    	while(!randState.compareAndSet(originalRandom, newRandom)){
    		originalRandom = randState.get();
    		newRandom = randPriority(originalRandom);
    	}
    	Node newNode = new Node(key, (int) newRandom);
    	addImplVoid(key, (int) newRandom, newNode);
    }

    private Node addImpl(final Node node, final int key, final int randValue) {
        if (node == null) {
            return new Node(key, randValue);
        }
//        else if (key == node.key) {
//            // no insert needed
//            return node;
//        }
        else if (key < node.key) {
            node.left = addImpl(node.left, key, randValue);
            if (randValue > node.priority) {
                return rotateRight(node);
            }
            return node;
        }
        else if (key > node.key){
            node.right = addImpl(node.right, key, randValue);
            if (randValue > node.priority) {
                return rotateLeft(node);
            }
            return node;
        }else {
        	// key == node.key
        	return node;
        }
    }   
    
    @Atomic
    private void addImplVoid(final int key, final int randValue, Node newNode) {
    	if(root == null) {
    		root = newNode;
    		return;
    	}
//    	if(root.key == key){
//    		return;
//    	}
//    	if(contains(key)){
//    		return;
//    	}
    	if(key < root.key){
    		if(root.left == null) {
    			root.left = newNode;
    			if(root.left.priority > root.priority){
    				root = rotateRight(root);
    			}
    		}else{
    			deepAddLeft(root,key, randValue, newNode);
    		}   		
    	}else if(key > root.key){
    		if(root.right == null) {
    			root.right = newNode;
    			if(root.right.priority > root.priority){
    				root = rotateLeft(root);
    			}
    		}else{
    			deepAddRight(root,key, randValue, newNode);
    		}
    	}
    	//deepAdd(root, key, randValue);
    }
    
    private void deepAdd(Node node, final int key, final int randValue){
    	if(key < node.key){
    		if(key < node.left.key){
    			if(node.left.left == null){
    				node.left = addImpl(node.left, key, randValue);
    			}else{
    	    		deepAdd(node.left,key, randValue);    				
    			}  			
    		}else if(key > node.left.key){
    			if(node.left.right == null){
    				node.left = addImpl(node.left, key, randValue);
    			}else{
    	    		deepAdd(node.left,key, randValue);    				
    			}
    		}  		
    	}else if(key > node.key){
    		if(key < node.right.key){
    			if(node.right.left == null){
    				node.right = addImpl(node.right, key, randValue);
    			}else{
    	    		deepAdd(node.right,key, randValue);    				
    			}  			
    		}else if (key > node.right.key){
    			if(node.right.right == null){
    				node.right = addImpl(node.right, key, randValue);
    			}else{
    	    		deepAdd(node.right,key, randValue);    				
    			}
    		}  		
    	}
    }
 
    @Atomic
    private void deepAddLeft(Node node, final int key, final int randValue, Node newNode){
   
    		if(key < node.left.key){
    			if(node.left.left == null){
    				node.left.left = newNode;
    				if(randValue > node.left.priority){
    					node.left = rotateRight(node.left);
    				}
//    				node.left = addImpl(node.left, key, randValue);
    			}else{
    				deepAddLeft(node.left,key, randValue, newNode);    				
    			}  			
    		}else if(key > node.left.key){
    			if(node.left.right == null){
    				node.left.right = newNode;
    				if(randValue > node.left.priority){
    					node.left = rotateLeft(node.left);
    				}
//    				node.left = addImpl(node.left, key, randValue);
    			}else{
    				deepAddRight(node.left,key, randValue, newNode);    				
    			}
    		}
    }
 
    @Atomic
    private void deepAddRight(Node node, final int key, final int randValue, Node newNode){
    		if(key < node.right.key){
    			if(node.right.left == null){
    				node.right.left = newNode;
    				if(randValue > node.right.priority){
    					node.right = rotateRight(node.right);
    				}
   // 				node.right = addImpl(node.right, key, randValue);
    			}else{
    				deepAddLeft(node.right,key, randValue, newNode);    				
    			}  			
    		}else if (key > node.right.key){
    			if(node.right.right == null){
    				node.right.right = newNode;
    				if(randValue > node.right.priority){
    					node.right = rotateLeft(node.right);
    				}
//    				node.right = addImpl(node.right, key, randValue);
    			}else{
    	    		deepAddRight(node.right,key, randValue, newNode);    				
    			}
    		}  		
    }
  
    
//    private void addImplNew(final int key) {  
////    	Node current;
//    	NodeState current;
//    	// Check if root is null
//    	current = checkAndAddRoot(key);
//    	if(current.cSuccess == true) return;
//    	
//    	// Find the lowest node for the add.  
//    	current = addTraverseReadOnly(root, key);        
//        if(current == null) {
//        	addImplNew(key);
//        	return;
//        }
//        
//        //current.cNode = root;
//        // Do the add. If the Node changes before the atomic state is reached, restart. 
//        //current = addNodeWrite(current, key);
//        //if(current.cSuccess == false) addImplNew(key);
//        current.cNode = addImpl(current.cNode, key);
//        //root = current.cNode;
//        
////        return;
//    }
//    
//    @Atomic 
//    private NodeState checkAndAddRoot(final int key){
//    	if(root == null){
//    		root = new Node(key, randPriority());
//    		return new NodeState(root, true);    		
//    	}else{
//    		return new NodeState(root, false);
//    	}
//    }    
//    
//    
////    private NodeState checkAndAdd(Node node, final int key){
////    	if(node == null){
////    		node = new Node(key, randPriority());
////    		return new NodeState(node, true);    		
////    	}else{
////    		return new NodeState(node, false);
////    	}
////    }
//    
//    @Atomic
//    private NodeState addTraverseReadOnly(final Node node, final int key) {
//    	
//    	if(node == null) { 
////    		return checkAndAddRoot(key);
//    		return null;
//    	}
//    	
//    	if(node.key == key){
//    		return new NodeState(node,true);
//    	}else if(key < node.key){
//    		if(node.left == null){
//    			return new NodeState(node,true);
//    		}else{
//    			return addTraverseReadOnly(node.left, key);
//    		}
//    	}else{
//    		if(node.right == null){
//    			return new NodeState(node,true);
//    		}else{
//    			return addTraverseReadOnly(node.right, key);
//    		}
//    	}
////    	return null;
//    }	
//    
//    @Atomic
//    private NodeState addNodeWrite(final NodeState ns, final int key) {
//    	if(ns.isChanged() == true){ 
//    		return new NodeState(ns.cNode, false);
//    	}else{
//    		ns.cNode = addImpl(ns.cNode, key);
//    		return new NodeState(ns.cNode, true);
//    	}
//        	
////    	if(ns.cNode.key == key) return new NodeState(ns.cNode, true);
////    	if (key<ns.cNode.key) {
////    		ns.cNode.left = checkAndAdd(ns.cNode.left, key).cNode;
////            if (ns.cNode.left.priority > ns.cNode.priority) {
////            	ns.cNode = rotateRight(ns.cNode);
////                return new NodeState(ns.cNode,true);
////            }
////    	} else {
////    		ns.cNode.right = checkAndAdd(ns.cNode.right, key).cNode;    	
////            if (ns.cNode.right.priority > ns.cNode.priority) {
////            	ns.cNode = rotateLeft(ns.cNode);
////                return new NodeState(ns.cNode,true);
////            }
////    	}
////    	return new NodeState(ns.cNode, true);
//    }
    
//    private int randPriority() {
//        // The constants in this 64-bit linear congruential random number
//        // generator are from http://nuclear.llnl.gov/CNP/rng/rngman/node4.html
//        randState = randState * 2862933555777941757L + 3037000493L;
//        return (int)(randState >> 30);
//    }

    private long randPriority(long randNum) {
        // The constants in this 64-bit linear congruential random number
        // generator are from http://nuclear.llnl.gov/CNP/rng/rngman/node4.html
    	randNum = randNum * 2862933555777941757L + 3037000493L;
        return (randNum >> 30);
    }
    
    private Node rotateRight(final Node node) {
        //       node                  nL
        //     /      \             /      \
        //    nL       z     ==>   x       node
        //  /   \                         /   \
        // x   nLR                      nLR   z
        final Node nL = node.left;
        node.left = nL.right;
        nL.right = node;
        return nL;
    }

    private Node rotateLeft(final Node node) {
        final Node nR = node.right;
        node.right = nR.left;
        nR.left = node;
        return nR;
    }

    public void remove(final int key) {
//        root = removeImpl(root, key);
    	removeImptVoid(key);
    }
    
    @Atomic
    private void removeImptVoid(final int key){    	
//    	if(root == null){
//    		return;
//    	}
    	if(contains(key)==false){
    		return;
    	}
    	if(root.key == key){
    		root = removeImpl(root, key);
    		return;
    	}
    	if(key < root.key){
    		if(root.left.key == key) {
    			root.left = removeImpl(root.left, key);
    			return;
    		}
    		deepRemoveLeft(root, key);
    	}else{
    		if(root.right.key == key) {
    			root.right = removeImpl(root.right, key);
    			return;
    		}    		
    		deepRemoveRight(root, key);
    	}
//    	deepRemove(root,key);
    }
    
    private void deepRemove(Node node,final int key){
    	if(key < node.key){
    		if(key < node.left.key){
    			if(node.left.left.key == key){
    				node.left.left = removeImpl(node.left.left, key);
    			}else{
    				deepRemove(node.left, key);
    			}
    		}else{
    			if(node.left.right.key == key){
    				node.left.right = removeImpl(node.left.right, key);
    			}else{
    				deepRemove(node.left, key);
    			}    			
    		}
    	}else{
    		if(key < node.right.key){
    			if(node.right.left.key == key){
    				node.right.left = removeImpl(node.right.left, key);
    			}else{
    				deepRemove(node.right, key);
    			}
    		}else{
    			if(node.right.right.key == key){
    				node.right.right = removeImpl(node.right.right, key);
    			}else{
    				deepRemove(node.right, key);
    			}    			
    		}    		
    	}
    }
   
    @Atomic
    private void deepRemoveLeft(Node node,final int key){
    		if(key < node.left.key){
    			if(node.left.left.key == key){
    				node.left.left = removeImpl(node.left.left, key);
    			}else{
    				deepRemoveLeft(node.left, key);
    			}
    		}else{
    			if(node.left.right.key == key){
    				node.left.right = removeImpl(node.left.right, key);
    			}else{
    				deepRemoveRight(node.left, key);
    			}    			
    		}
    	
    }
   
    @Atomic
    private void deepRemoveRight(Node node,final int key){
    		if(key < node.right.key){
    			if(node.right.left.key == key){
    				node.right.left = removeImpl(node.right.left, key);
    			}else{
    				deepRemoveLeft(node.right, key);
    			}
    		}else{
    			if(node.right.right.key == key){
    				node.right.right = removeImpl(node.right.right, key);
    			}else{
    				deepRemoveRight(node.right, key);
    			}    			
    		}    		
    }
    
    
//    private void removeImplNew(final int key) {
//    	NodeState current;
//    	current = removeTraverseReadOnly(root, key);
//    	if(current == null){
//    		return;
//    	}
//    	current = removeNodeWrite(current, key);
//    	if(current.cSuccess == false){
//    		removeImplNew(key);
//    	}
//    }
//
//    @Atomic
//    private NodeState removeTraverseReadOnly(final Node node, final int key) {
//    	if(node == null){
//    		return null;
//    	}
//    	if(node.key == key){
//    		return new NodeState(node, true);
//    	}else if(key < node.key){
//    		return removeTraverseReadOnly(node.left, key);
//    	}else {
//    		return removeTraverseReadOnly(node.right, key);
//    	}
//    }
//  
//    @Atomic 
//    private NodeState removeNodeWrite(NodeState ns, final int key){
//    	// Check if the node has changed.
//    	if(ns.isChanged()==true) {
//    		return new NodeState(ns.cNode, false);
//    	}else{
//    		ns.cNode = removeImpl(ns.cNode, key);
//    		return new NodeState(ns.cNode, true);
//    	}
//    	
//    }
    
    private Node removeImpl(final Node node, final int key) {
//        if (node == null) {
//            // not present, nothing to do
//            return null;
//        }
//        else if (key == node.key) {
            if (node.left == null) {
                // splice out this node
                return node.right;
            }
            else if (node.right == null) {
                return node.left;
            }
            else {
                // Two children, this is the hardest case.  We will pretend
                // that node has -infinite priority, move it down, then retry
                // the removal.
                if (node.left.priority > node.right.priority) {
                    // node.left needs to end up on top
                    final Node top = rotateRight(node);
                    top.right = removeImpl(top.right, key);
                    return top;
                } else {
                    final Node top = rotateLeft(node);
                    top.left = removeImpl(top.left, key);
                    return top;
                }
            }
//        }
//        else if (key < node.key) {
//            node.left = removeImpl(node.left, key);
//            return node;
//        }
//        else {
//            node.right = removeImpl(node.right, key);
//            return node;
//        }
    }
    
   
}
