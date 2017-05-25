package utils;

import java.io.Serializable;

public class ByteFormat implements Serializable{

	private static final long serialVersionUID = -803795265829278983L;
	
	private long bytes;
	private long kilos;
	private long megas;
	private long gigas;
	private long teras;

	private long yplot;
	
	public ByteFormat(long bytes, long yplot){
		this.bytes = bytes;
		this.kilos = ( bytes / 1000 );
		this.megas = ( this.kilos / 1000 );
		this.gigas = ( this.megas / 1000 );
		this.teras = ( this.gigas / 1000 );
		this.yplot = yplot;
	}
	
	public long getY(){
		return this.yplot;
	}
	
	public long getBytes(){
		return this.bytes;
	}
	
	public String toString(){
		
		if ( this.teras != 0)
			return this.teras+","+this.gigas % 1000+"To";
		
		if ( this.gigas != 0)
			return this.gigas+","+this.megas % 1000+"Go";
		
		if ( this.megas != 0)
			return this.megas+","+this.kilos % 1000+"Mo";
		
		if ( this.kilos != 0)
			return this.kilos+","+this.bytes % 1000+"Ko";
		
		return this.bytes+"B";
	}
}
