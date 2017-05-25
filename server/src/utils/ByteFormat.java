package utils;

public class ByteFormat {
	private long bytes;
	private long kilos;
	private long megas;
	private long gigas;
	private long teras;

	public ByteFormat(long bytes){
		this.bytes = bytes;
		this.kilos = ( bytes / 1000 );
		this.megas = ( this.kilos / 1000 );
		this.gigas = ( this.megas / 1000 );
		this.teras = ( this.gigas / 1000 );
	}
	
	public String toString(){
		
		if ( this.teras != 0)
			return this.teras+","+this.gigas % 1000+"T";
		
		if ( this.gigas != 0)
			return this.gigas+","+this.megas % 1000+"G";
		
		if ( this.megas != 0)
			return this.megas+","+this.kilos % 1000+"M";
		
		if ( this.kilos != 0)
			return this.kilos+","+this.bytes % 1000+"K";
		
		return this.bytes+"B";
	}
}
