package cf;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
/**
 * @author Federico Borsati
 * @bug: codice poco efficente per la lettura dal csv, in quanto sarebbe utile dividere i csv per citta' e scorrere quindi pochi valori rispetto a quelli attuali
 * @bug: non funzionante per utenti esteri
 */

public class CodiceFiscale {

    private String name, sur, birth, bCity, province, currentPath;
    private char sex;

    private static final String COMMA_DELIMITER = ",";

    /**
     * This is the parametized constructor.
     * @param name Name person.
     * @param sur Surname person.
     * @param birth Birth date, as [dd/mm/yyyy].
     * @param bCity Birth city.
     * @param sex Binary sex.
     * @param province Birth province.
     * @throws IOException Caused by finding current path.
     */
    public CodiceFiscale(String name, String sur, String birth, String bCity, char sex, String province) throws IOException{
        this.name = name;
        this.sur = sur;
        this.birth = birth;
        this.bCity = bCity;
        this.sex = sex;
        this.province = province;
        currentPath = new java.io.File(".").getCanonicalPath() + "/cf";
    }

    private boolean validBirth(String birth){
		if(birth.isEmpty() || birth.length() != 10){
			return false;
		}
		final Pattern pattern = Pattern.compile("^(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]))\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[13-9]|1[0-2])\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:29(\\/|-|\\.)0?2\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$");
		Matcher matcher = pattern.matcher(birth);
		boolean resMatch = matcher.find();
		if(!resMatch){
			return false;
		}
		else{
			return true;
		}
	}

    private boolean findProvince(String province) throws FileNotFoundException,IOException{
        if(province.isEmpty() || province.length() != 2){
			return false;
		}
		else{
			try (BufferedReader br = new BufferedReader(new FileReader(currentPath + "/data/lista.csv"))) {
				String line;
				while ((line = br.readLine()) != null) {
					String[] values = line.split(COMMA_DELIMITER);
                    if(values[1].toLowerCase().equals(province.toLowerCase())){
                        return true;                        
                    }
				}
			}
		}
        return false;
    }

	private String findCity(String dropCity) throws FileNotFoundException,IOException,CityNotFoundException{
		if(dropCity.isEmpty() || dropCity.length() < 3){
			throw new CityNotFoundException("Citta' formattata male");
		}
		else{
			try (BufferedReader br = new BufferedReader(new FileReader(currentPath + "/data/lista.csv"))) {
				String line;
				while ((line = br.readLine()) != null) {
					String[] values = line.split(COMMA_DELIMITER);
                    if(values[0].toLowerCase().equals(dropCity.toLowerCase())) {
                        return values[2];
                    }
				}
			}
		}
		throw new CityNotFoundException("Citta' formattata male o estera!");
	}

    private char findMonthCode(String month) throws FileNotFoundException,IOException{
        try (BufferedReader br = new BufferedReader(new FileReader(currentPath + "/data/month.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);
                if(values[1].equals(month)) {
                    return values[0].charAt(0);                   
                }
            }
        }
        return '?';
    }

    //Devo per forza usare la ricorsione, altrimenti non riesco ad eliminare le consonanti e quindi ad ottimizzare il codice.
    private String consonant(String str, boolean name){
        StringBuilder res = new StringBuilder();
        str = str.toLowerCase();
        String volw = "";

        for(int i=0; i<str.length(); i++) {
            if(str.charAt(i) == 'a'|| str.charAt(i) == 'e'|| str.charAt(i) == 'i' || str.charAt(i) == 'o' || str.charAt(i) == 'u'){
                   volw += str.charAt(i);
            } 
            else{
                res.append(str.charAt(i));
            }
        }
        if(res.length() > 3 ){
            if(name){
                res.deleteCharAt(1);
            }
            return res.substring(0, 3).toUpperCase();
        }
        else{
            if(res.length() == 2){
                if(volw.length()>0){
                    return res.append(volw.charAt(0)).toString().toUpperCase();
                }
                else{
                    return res.append("X").toString().toUpperCase();
                }
            }
            else if(res.length() == 1){
                if(volw.length()>1){
                    return res.append(volw.charAt(0)).append(volw.charAt(1)).toString().toUpperCase();
                }
                else if(volw.length() > 0){
                    return res.append(volw.charAt(0)).append("X").toString().toUpperCase();
                }
                else{
                    return res.append("XX").toString().toUpperCase();
                }
            }
            else{
                return null;
            }
        }
    }

    private String birthSex(String birth, char sex){
        int bInt = Integer.parseInt(birth);
        if(Character.toLowerCase(sex) == 'f'){
            return Integer.toString(bInt+40);
        }
        else{
            return birth;
        }
    }

    private int cinConversion(boolean even, char chr) throws FileNotFoundException,IOException{
        if(even){
            try (BufferedReader br = new BufferedReader(new FileReader(currentPath + "/data/cin_pari.csv"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(COMMA_DELIMITER);
                    if(values[0].charAt(0) == chr) {
                        return Integer.parseInt(values[1]);                   
                    }
                }
            }
        }
        else{
            try (BufferedReader br = new BufferedReader(new FileReader(currentPath + "/data/cin_disp.csv"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(COMMA_DELIMITER);
                    if(values[0].charAt(0) == chr) {
                        return Integer.parseInt(values[1]);                   
                    }
                }
            }
        }
        return -1;
    }

    private String cin(String partCf) throws FileNotFoundException,IOException{
        int odd = 0, even = 0;
        for(int i=1; i<partCf.length()+1; i++){
            if(i%2 == 0){
                even += cinConversion(true,partCf.charAt(i-1));
            }
            else{
                odd += cinConversion(false,partCf.charAt(i-1));
            }
            
        }

        //even, now alias as res
        even += odd;
        even %= 26;
        try (BufferedReader br = new BufferedReader(new FileReader(currentPath + "/data/cin_resto.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);
                if(Integer.parseInt(values[0]) == even) {
                    return values[1];                   
                }
            }
        }
        return "-1";
    }

    /**
     * A method for generating italian fiscal code.
     * @return The italian fiscal code as String.
     * @throws FormatException Thrown when one of parameters is bad formatted.
     * @throws CityNotFoundException Thrown when parameter bCity was not found.
     * @throws FileNotFoundException Thrown when csv's aren't been found.
     * @throws IOException Thrown when some problems occur on loading external files.
     */
	public String genCf() throws FormatException,CityNotFoundException,FileNotFoundException,IOException{
		name = name.trim();
		sur = sur.trim();
		birth = birth.trim();
		bCity = bCity.trim();

		if(name.isEmpty() || name.length() < 3 || sur.isEmpty() || sur.length() < 3){
			throw new FormatException();
		}
		else if(!validBirth(birth)){
			throw new FormatException("Birth not accepted.");
		}
        else if(!findProvince(province)){
            throw new CityNotFoundException("Province not found.");
        }
        else if(Character.toLowerCase(sex) != 'm' && Character.toLowerCase(sex) != 'f'){
            throw new FormatException("Binary sex not provided.");
        }
		String codCatasto = findCity(bCity);

        String shtName = consonant(name, true);
        String shtSur = consonant(sur, false);
        String year = "" + birth.charAt(8) + birth.charAt(9);
        char monthCode = findMonthCode("" + birth.charAt(3) + birth.charAt(4));
        String birthFmt = birthSex("" + birth.charAt(0) + birth.charAt(1), sex);

        String res = "" + shtSur + shtName + year  + monthCode + birthFmt + codCatasto;
        String cin = cin(res);
        res += cin;

		return res;
	}

    //Get method
    public String getName(){
        return name;
    }
    public String getSur(){
        return sur;
    }
    public String getBirth(){
        return birth;
    }
    public String getBCity(){
        return bCity;
    }
    public char getSex(){
        return sex;
    }
    public String getProv(){
        return province;
    }

    public String toString(){
        Map<String, String> map = new HashMap<String, String>();
        map.put("Name", name);
        map.put("Surname", sur);
        map.put("Birth", birth);
        map.put("Birth city", bCity);
        map.put("Sex", sex + "");
        map.put("Province", province);
        return map.toString();
    }
}
