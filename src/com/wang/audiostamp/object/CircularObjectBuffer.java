package com.wang.audiostamp.object;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

public class CircularObjectBuffer {
	 
    /**
     * The default size for a circular object buffer.
     *
     * @since ostermillerutils 1.00.00
     */
    private final static int DEFAULT_SIZE = 1024;
 
    /**
     * A buffer that will grow as things are added.
     *
     * @since ostermillerutils 1.00.00
     */
    public final static int INFINITE_SIZE = -1;
 
    /**
     * The circular buffer.
     * <p>
     * The actual capacity of the buffer is one less than the actual length
     * of the buffer so that an empty and a full buffer can be
     * distinguished.  An empty buffer will have the readPostion and the
     * writePosition equal to each other.  A full buffer will have
     * the writePosition one less than the readPostion.
     * <p>
     * There are two important indexes into the buffer:
     * The readPosition, and the writePosition. The Objects
     * available to be read go from the readPosition to the writePosition,
     * wrapping around the end of the buffer.  The space available for writing
     * goes from the write position to one less than the readPosition,
     * wrapping around the end of the buffer.
     *
     * @since ostermillerutils 1.00.00
     */
    protected byte[] buffer;
    /**
     * Index of the first Object available to be read.
     *
     * @since ostermillerutils 1.00.00
     */
    protected volatile int readPosition = 0;
    /**
     * Index of the first Object available to be written.
     *
     * @since ostermillerutils 1.00.00
     */
    protected volatile int writePosition = 0;
    protected boolean isEverOverflow = false;
    /**
     * Make this buffer ready for reuse.  The contents of the buffer
     * will be cleared and the streams associated with this buffer
     * will be reopened if they had been closed.
     *
     * @since ostermillerutils 1.00.00
     */
    public void clear(){
        synchronized (this){
            readPosition = 0;
            writePosition = 0;
            isEverOverflow = false;
        }
    }
 
    private boolean isEverOverflow(){
        synchronized (this){
            return isEverOverflow;
        }
    }
    
    public int getWriteLength(){
    	synchronized (this){
    		if(isEverOverflow()){
    			return buffer.length;
    		} else {
    			return writePosition;
    		}
    	}
    }
 
    /**
     * Get the capacity of this buffer.
     * <p>
     * Note that the number of Objects available plus
     * the number of Objects free may not add up to the
     * capacity of this buffer, as the buffer may reserve some
     * space for other purposes.
     *
     * @return the size in Objects of this buffer
     *
     * @since ostermillerutils 1.00.00
     */
    public int getSize(){
        synchronized (this){
            return buffer.length;
        }
    }
 
    @SuppressWarnings("unchecked") private byte[] createArray(int size){
        return (byte[]) new byte[size];
    }
 
    /**
     * double the size of the buffer
     *
     * @since ostermillerutils 1.00.00
     */
    private void resize(){
        byte[] newBuffer = createArray(buffer.length * 2);
        int available = available();
        if (readPosition <= writePosition){
            // any space between the read and
            // the first write needs to be saved.
            // In this case it is all in one piece.
            int length = writePosition - readPosition;
            System.arraycopy(buffer, readPosition, newBuffer, 0, length);
        } else {
            int length1 = buffer.length - readPosition;
            System.arraycopy(buffer, readPosition, newBuffer, 0, length1);
            int length2 = writePosition;
            System.arraycopy(buffer, 0, newBuffer, length1, length2);
        }
        buffer = newBuffer;
        readPosition = 0;
        writePosition = available;
    }
 
    /**
     * Space available in the buffer which can be written.
     *
     * @since ostermillerutils 1.00.00
     */
    private int spaceLeft(){
        if (writePosition < readPosition){
            // any space between the first write and
            // the read except one Object is available.
            // In this case it is all in one piece.
            return (readPosition - writePosition - 1);
        }
        // space at the beginning and end.
        return ((buffer.length - 1) - (writePosition - readPosition));
    }
 
    /**
     * Objects available for reading.
     *
     * @since ostermillerutils 1.00.00
     */
    private int available(){
        if (readPosition <= writePosition){
            // any space between the first read and
            // the first write is available.  In this case i
            // is all in one piece.
            return (writePosition - readPosition);
        }
        // space at the beginning and end.
        return (buffer.length - (readPosition - writePosition));
    }
 
    /**
     * Create a new buffer with a default capacity.
     * Writing to a full buffer will block until space
     * is available rather than throw an exception.
     *
     * @since ostermillerutils 1.00.00
     */
    public CircularObjectBuffer(){
        this (DEFAULT_SIZE, true);
    }
 
    /**
     * Create a new buffer with given capacity.
     * Writing to a full buffer will block until space
     * is available rather than throw an exception.
     * <p>
     * Note that the buffer may reserve some Objects for
     * special purposes and capacity number of Objects may
     * not be able to be written to the buffer.
     * <p>
     * Note that if the buffer is of INFINITE_SIZE it will
     * neither block or throw exceptions, but rather grow
     * without bound.
     *
     * @param size desired capacity of the buffer in Objects or CircularObjectBuffer.INFINITE_SIZE.
     *
     * @since ostermillerutils 1.00.00
     */
    public CircularObjectBuffer(int size){
        this (size, true);
    }
 
    /**
     * Create a new buffer with a default capacity and
     * given blocking behavior.
     *
     * @param blockingWrite true writing to a full buffer should block
     *        until space is available, false if an exception should
     *        be thrown instead.
     *
     * @since ostermillerutils 1.00.00
     */
    public CircularObjectBuffer(boolean blockingWrite){
        this (DEFAULT_SIZE, blockingWrite);
    }
 
    /**
     * Create a new buffer with the given capacity and
     * blocking behavior.
     * <p>
     * Note that the buffer may reserve some Objects for
     * special purposes and capacity number of Objects may
     * not be able to be written to the buffer.
     * <p>
     * Note that if the buffer is of INFINITE_SIZE it will
     * neither block or throw exceptions, but rather grow
     * without bound.
     *
     * @param size desired capacity of the buffer in Objects or CircularObjectBuffer.INFINITE_SIZE.
     * @param blockingWrite true writing to a full buffer should block
     *        until space is available, false if an exception should
     *        be thrown instead.
     *
     * @since ostermillerutils 1.00.00
     */
    public CircularObjectBuffer(int size, boolean blockingWrite){
        if (size == INFINITE_SIZE){
            buffer = createArray(DEFAULT_SIZE);
        } else {
            buffer = createArray(size);
        }
    }
 
 
    /**
     * Get a single Object from this buffer.  This method should be called
     * by the consumer.
     * This method will block until a Object is available or no more
     * objects are available.
     *
     * @return The Object read, or null if there are no more objects
     * @throws InterruptedException if the thread is interrupted while waiting.
     *
     * @since ostermillerutils 1.00.00
     */
    public byte read() throws InterruptedException {
        while (true){
            synchronized (this){
                int available = available();
                if (available > 0){
                    byte result = buffer[readPosition];
                    readPosition++;
                    if (readPosition == buffer.length){
                        readPosition = 0;
                    }
                    return result;
                } 
            }
            Thread.sleep(100);
        }
    }
 
    /**
     * Get Objects into an array from this buffer.  This method should
     * be called by the consumer.
     * This method will block until some input is available,
     * or there is no more input.
     *
     * @param buf Destination buffer.
     * @return The number of Objects read, or -1 there will
     *     be no more objects available.
     * @throws InterruptedException if the thread is interrupted while waiting.
     *
     * @since ostermillerutils 1.00.00
     */
    public int read(byte[] buf) throws InterruptedException {
        return read(buf, 0, buf.length);
    }
 
    /**
     * Get Objects into a portion of an array from this buffer.  This
     * method should be called by the consumer.
     * This method will block until some input is available,
     * an I/O error occurs, or the end of the stream is reached.
     *
     * @param buf Destination buffer.
     * @param off Offset at which to start storing Objects.
     * @param len Maximum number of Objects to read.
     * @return The number of Objects read, or -1 there will
     *     be no more objects available.
     * @throws InterruptedException if the thread is interrupted while waiting.
     *
     * @since ostermillerutils 1.00.00
     */
    public int read(byte[] buf, int off, int len) throws InterruptedException {
        while (true){
            synchronized (this){
                //int available = available();
                //if (available > 0){
                    int length = len;
                    boolean isOverflow = length>buffer.length - readPosition;
                    int firstLen = isOverflow?buffer.length - readPosition:length;
                    int secondLen = isOverflow?length - firstLen:0;
                    System.arraycopy(buffer, readPosition, buf, off, firstLen);
                    if (secondLen > 0){
                        System.arraycopy(buffer, 0, buf, off+firstLen,  secondLen);
                        readPosition = secondLen;
                    } else {
                        readPosition += length;
                    }
                    if (readPosition == buffer.length) {
                        readPosition = 0;
                    }
                    return length;
                /*} else if (inputDone){
                    return -1;
                }*/
            }
            //Thread.sleep(100);
        }
    }
 
 
    /**
     * Skip Objects.  This method should be used by the consumer
     * when it does not care to examine some number of Objects.
     * This method will block until some Objects are available,
     * or there will be no more Objects available.
     *
     * @param n The number of Objects to skip
     * @return The number of Objects actually skipped
     * @throws IllegalArgumentException if n is negative.
     * @throws InterruptedException if the thread is interrupted while waiting.
     *
     * @since ostermillerutils 1.00.00
     */
    public long skip(long n) throws InterruptedException, IllegalArgumentException {
        while (true){
            synchronized (this){
                int available = available();
                if (available > 0){
                    int length = Math.min((int)n, available);
                    int firstLen = Math.min(length, buffer.length - readPosition);
                    int secondLen = length - firstLen;
                    if (secondLen > 0){
                        readPosition = secondLen;
                    } else {
                        readPosition += length;
                    }
                    if (readPosition == buffer.length) {
                        readPosition = 0;
                    }
                    return length;
                } 
            }
            Thread.sleep(100);
        }
    }
 
    /**
     * Fill this buffer with array of Objects.  This method should be called
     * by the producer.
     * If the buffer allows blocking writes, this method will block until
     * all the data has been written rather than throw a BufferOverflowException.
     *
     * @param buf Array of Objects to be written
     * @throws BufferOverflowException if buffer does not allow blocking writes
     *   and the buffer is full.  If the exception is thrown, no data
     *   will have been written since the buffer was set to be non-blocking.
     * @throws IllegalStateException if done() has been called.
     * @throws InterruptedException if the write is interrupted.
     *
     * @since ostermillerutils 1.00.00
     */
    public void write(byte[] buf) throws BufferOverflowException, IllegalStateException, InterruptedException {
        write(buf, 0, buf.length);
    }
 
    /**
     * Fill this buffer with a portion of an array of Objects.
     * This method should be called by the producer.
     * If the buffer allows blocking writes, this method will block until
     * all the data has been written rather than throw an IOException.
     *
     * @param buf Array of Objects
     * @param off Offset from which to start writing Objects
     * @param len - Number of Objects to write
     * @throws BufferOverflowException if buffer does not allow blocking writes
     *   and the buffer is full.  If the exception is thrown, no data
     *   will have been written since the buffer was set to be non-blocking.
     * @throws IllegalStateException if done() has been called.
     * @throws InterruptedException if the write is interrupted.
     *
     * @since ostermillerutils 1.00.00
     */
    public void write(byte[] buf, int off, int len) throws BufferOverflowException, IllegalStateException, InterruptedException {
        while (len > 0){
            synchronized (CircularObjectBuffer.this){
                /*if (inputDone) throw new IllegalStateException("CircularObjectBuffer.done() has been called, CircularObjectBuffer.write() failed.");
                int spaceLeft = spaceLeft();
                while (infinite && spaceLeft < len){
                    resize();
                    spaceLeft = spaceLeft();
                }
                if (!blockingWrite && spaceLeft < len) throw new BufferOverflowException();
                */
            	//int realLen = Math.min(len, spaceLeft);
            	int spaceLeft = buffer.length - writePosition;
            	boolean isOverflow = spaceLeft < buf.length;
                int firstLen = isOverflow?spaceLeft:buf.length;
                int secondLen = 0;
                if(isOverflow){
                	secondLen = buf.length - firstLen;
                	
                }
                int written = firstLen + secondLen;
                if (firstLen > 0){
                    System.arraycopy(buf, off, buffer, writePosition, firstLen);
                }
                if (secondLen > 0){
                    System.arraycopy(buf, off+firstLen, buffer, 0, secondLen);
                    writePosition = secondLen;
                    readPosition = secondLen;
                    isEverOverflow = true;
                } else {
                    writePosition += written;
                }
                if (writePosition == buffer.length) {
                    writePosition = 0;
                }
                off += written;
                len -= written;
            }
            /*if (len > 0){
                Thread.sleep(100);
            }*/
        }
    }    
}