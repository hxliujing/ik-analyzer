package org.wltea.analyzer;

import java.util.HashSet;
import java.util.Set;

import org.wltea.analyzer.seg.ISegmenter;

/**
 * 分词器上下文状态
 * @author 林良益
 *
 */
public class Context{
	
    //记录Reader内已分析的字串总长度
    //在分多段分析词元时，该变量累计当前的segmentBuff相对于reader的位移
	private int buffOffset;	
	//最近一次读入的,可处理的字串长度
	private int available;
    //最近一次分析的字串长度
    private int lastAnalyzed;	
    //当前缓冲区位置指针
    private int cursor; 
    /*
     * 记录正在使用buffer的分词器对象
     * 如果set中存在有分词器对象，则buffer不能进行位移操作（处于locked状态）
     */
    private Set<ISegmenter> buffLocker;
    /*
     * 词元结果集，为每次游标的移动，存储切分出来的词元
     */
	private IKSortedLinkSet lexemeSet;

    
    Context(){
    	buffLocker = new HashSet<ISegmenter>(4);
    	lexemeSet = new IKSortedLinkSet();
	}

	public int getBuffOffset() {
		return buffOffset;
	}


	public void setBuffOffset(int buffOffset) {
		this.buffOffset = buffOffset;
	}

	public int getLastAnalyzed() {
		return lastAnalyzed;
	}


	public void setLastAnalyzed(int lastAnalyzed) {
		this.lastAnalyzed = lastAnalyzed;
	}


	public int getCursor() {
		return cursor;
	}


	public void setCursor(int cursor) {
		this.cursor = cursor;
	}
	
	public void lockBuffer(ISegmenter segmenter){
		this.buffLocker.add(segmenter);
	}
	
	public void unlockBuffer(ISegmenter segmenter){
		this.buffLocker.remove(segmenter);
	}
	
	/**
	 * 只要buffLocker中存在ISegmenter对象
	 * 则buffer被锁定
	 * @return
	 */
	public boolean isBufferLocked(){
		return this.buffLocker.size() > 0;
	}

	public int getAvailable() {
		return available;
	}

	public void setAvailable(int available) {
		this.available = available;
	}

	/**
	 * 取出分词结果集中的首个词元
	 * @return
	 */
	public Lexeme firstLexeme() {
		return this.lexemeSet.pollFirst();
	}
	
	/**
	 * 取出分词结果集中的最后一个词元
	 * @return
	 */
	public Lexeme lastLexeme() {
		return this.lexemeSet.pollLast();
	}
	
	/**
	 * 向分词结果集添加词元
	 * @param lexeme
	 */
	public void addLexeme(Lexeme lexeme){
		this.lexemeSet.addLexeme(lexeme);
	}
	
	/**
	 * 获取分词结果集大小
	 * @return
	 */
	public int getResultSize(){
		return this.lexemeSet.size();
	}
	
	/**
	 * 
	 * @author linly
	 *
	 */
	private class IKSortedLinkSet{
		//链表头
		private Lexeme head;
		//链表尾
		private Lexeme tail;
		//链表的实际大小
		private int size;
		
		private IKSortedLinkSet(){
			this.size = 0;
		}
		/**
		 * 向链表集合添加词元
		 * @param lexeme
		 */
		private void addLexeme(Lexeme lexeme){
			if(this.size == 0){
				this.head = lexeme;
				this.tail = lexeme;
				this.size++;
				return;
				
			}else{
				if(this.tail.compareTo(lexeme) == 0){//词元与尾部词元相同，不放入集合
					return;
					
				}else if(this.tail.compareTo(lexeme) < 0){//词元接入链表尾部
					this.tail.setNext(lexeme);
					lexeme.setPrev(this.tail);
					this.tail = lexeme;
					this.size++;
					return;
					
				}else if(this.head.compareTo(lexeme) > 0){//词元接入链表头部
					this.head.setPrev(lexeme);
					lexeme.setNext(this.head);
					this.head = lexeme;
					this.size++;
					return;
					
				}else{					
					//从尾部上逆
					Lexeme l = this.tail;
					while(l != null && l.compareTo(lexeme) > 0){
						l = l.getPrev();
					}
					if(l.compareTo(lexeme) == 0){//词元与集合中的词元重复，不放入集合
						return;
						
					}else if(l.compareTo(lexeme) < 0){//词元插入链表中的某个位置
						lexeme.setPrev(l);
						lexeme.setNext(l.getNext());
						l.getNext().setPrev(lexeme);
						l.setNext(lexeme);
						this.size++;
						return;
						
					}
				}
			}
			
		}
		/**
		 * 取出链表集合的第一个元素
		 * @return Lexeme
		 */
		private Lexeme pollFirst(){
			if(this.size == 1){
				Lexeme first = this.head;
				this.head = null;
				this.tail = null;
				this.size--;
				return first;
			}else if(this.size > 1){
				Lexeme first = this.head;
				this.head = first.getNext();
				first.setNext(null);
				this.size --;
				return first;
			}else{
				return null;
			}
		}
		
		/**
		 * 取出链表集合的最后一个元素
		 * @return Lexeme
		 */
		private Lexeme pollLast(){
			if(this.size == 1){
				Lexeme last = this.head;
				this.head = null;
				this.tail = null;
				this.size--;
				return last;
				
			}else if(this.size > 1){
				Lexeme last = this.tail;
				this.tail = last.getPrev();
				last.setPrev(null);
				this.size--;
				return last;
				
			}else{
				return null;
			}
		}		
		
		private int size(){
			return this.size;
		}
	}
}
