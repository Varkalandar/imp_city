package impcity.uikit;

import java.text.NumberFormat;

/**
 *
 * @author Hj. Malthaner
 */
public class StringUtils 
{
    private static final NumberFormat nf = NumberFormat.getInstance();
    
    
    public static final String upperCaseFirst(String word)
    {
        return word.substring(0, 1).toUpperCase() +
                word.substring(1);
    }
    
    
    public static String setDecimal(int base, int position)
    {
        StringBuilder stb = new StringBuilder();
        stb.append(base);
        
        int l = stb.length();
        
        if(l - position > 0)
        {
            stb.insert(l - position, '.');
        }
        else
        {
            stb.insert(l - position, "0.");
        }
        return stb.toString();
    }
    
    public static String calcWeightString(int grams)
    {
        if(grams < 100)
        {
            return "" + grams + " g";
        }
        else
        {
            return StringUtils.setDecimal(grams/100, 1) + " kg";
        }
    }
    
    public static String calcBurdenString(int burden)
    {
        nf.setMaximumFractionDigits(0);
        return nf.format(burden);
    }

    public static String zeroPad(int number, int digits) 
    {
        String s = "" + number;
        
        int l = s.length();
        
        StringBuilder b = new StringBuilder();        
        for(int i=0; i < digits-l; i++)
        {
            b.insert(0, '0');
        }
        
        b.append(s);
        
        return b.toString();
    }
}
