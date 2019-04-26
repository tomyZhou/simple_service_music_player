import java.math.BigDecimal;

public class Hello {


    public static void main(String[] args) {
        BigDecimal result1 = new BigDecimal(Double.toString(1.21333));
        String str = result1.setScale(1, BigDecimal.ROUND_UP).toPlainString() + "KB";

        System.out.println(str);

    }

}
