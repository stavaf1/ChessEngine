package Test;

public class CreatingBitMasks {

    /**---------------------------------------\
     * FLAGS FOR SPECIAL MOVE TYPES
     * n = normal move,
     * p = double push,
     * l = king castle,
     * c = queen castle,
     * x = captures,
     * e = en passant
     * k = knight promotion //any of these with capture simply capitalised
     * b = bishop promotion
     * r = rook promotion
     * q = queen promotion
     */


    public static void main(String[] args){
//        System.out.println(stringToBinary("0000000011111111000000000000000000000000000000000000000000000000"));
        System.out.println("rank 1 should look liek");
        System.out.println(stringToBinary("1000000010000000100000001000000010000000100000001000000010000000"));
        System.out.println("rank 2 should look like");
        System.out.println(Long.toBinaryString(stringToBinary("0000000100000001000000010000000100000001000000010000000100000001")));
        System.out.println("rank 2 looks like");
        System.out.println(Long.toBinaryString(72340172838076672L));



    }
    public static long stringToBinary(String bitboard)
    {
        if(bitboard.charAt(0) == '0'){
            return Long.parseLong(bitboard, 2); //converts to base 2
        } else {
            return Long.parseLong("1" + bitboard.substring(2),2) * 2;
        }
    }
}
