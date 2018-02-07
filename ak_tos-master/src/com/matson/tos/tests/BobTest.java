package com.matson.tos.tests;

import com.matson.tos.processor.MdbAcetsMessageProcessor;
import com.matson.tos.util.StrUtil;

import java.io.*;
import java.util.*;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
public class BobTest {
	private static StringWriter out;
	/**
	 * @param args
	 */
/*	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String msgText = null;
		MdbAcetsMessageProcessor mdb = new MdbAcetsMessageProcessor();
		mdb.processMsg(msgText);
	}
*/
	
/*	public static void main(String[] args) {

	    File file = new File("C:/A_INSTALL/UNIT_LOAD/UNIT_LOAD.txt");
	    FileInputStream fis = null;
	    BufferedInputStream bis = null;
	    DataInputStream dis = null;

	    try {
	      fis = new FileInputStream(file);

	      // Here BufferedInputStream is added for fast reading.
	      bis = new BufferedInputStream(fis);
	      dis = new DataInputStream(bis);

	      // dis.available() returns 0 if the file does not have more lines.
	      while (dis.available() != 0) {

	      // this statement reads the line from the file and print it to
	        // the console.
	        System.out.println(dis.readLine());
	      }

	      // dispose all the resources after using them.
	      fis.close();
	      bis.close();
	      dis.close();

	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	  }
	  
*/

	public static void main( String[] args)
	{
		try
		{
			File file = new File("C:/A_INSTALL/BOB.xml");
			
			BufferedReader br = new BufferedReader(new FileReader(file));
			BufferedReader bRead2 = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line = null;
			String outfile = new String();
			StringBuffer strBuff = new StringBuffer();
			ArrayList<String> eqList = new ArrayList<String>();
			
			//out = new StringWriter();

			/*while ((line = br.readLine()) != null )
			{
				String strOut = eventSpecificFieldValue(line,"aTime=","12:25:00");
				String ctrNo = StrUtil.getFieldValue(line,"ctrNo=");
				if(getNbr(ctrNo)){
				 sb.append(strOut + "\n");
				}
				String eqnub = line;
				eqList.add(eqnub);
			}*/
			
			while ((line = bRead2.readLine()) != null ){
				
				String temp = line;
				send(temp);
				System.out.println("ONE UNIT");
				//String eqNbr = line.trim();
				//String modEqNbr = eqNbr.substring(0,eqNbr.length()-1);

				
			   /* Iterator<String> itr = getNbr("").iterator();
			    while (itr.hasNext()) {
			      String element = itr.next();
			      
			      
			      if(line.contains(element)){
			    	  System.out.println("CNTR NBR="+element);
			    	  sb.append(line+"\n");
			      } */
			     /* if(eqNbr.equals(element)){
			    	  //sb.append(element+"  "+eqNbr+ "\n");
			    	  continue;
			      }//-- To Find out Different check digits  
			      else if(element.length()-1 == modEqNbr.length() && element.contains(modEqNbr)){
                       sb.append(element+"  "+eqNbr+ "\n");			    	  
			      }
			     //-- To Find Out Short Numbers 
			      else if(eqNbr.substring(0,eqNbr.length()-1).equals(element)){
			    	  System.out.println(element+"  "+eqNbr+ "\n");
			    	  sb.append(element+"  "+eqNbr+ "\n");
			      }*/
			   //}
			    

				
			}

				br.close();
				bRead2.close();
				//out.close();
				/* 
				 String fileStr = sb.toString();
				//DcmConverterFileProcessor dcmConverter = new DcmConverterFileProcessor();
				//System.out.println("Before processFile(fileStr)");
				//dcmConverter.processFile(fileStr);
				//System.out.println("After processFile(fileStr)");
				
				File fileOut = new File("C:/A_INSTALL/MKA209_FILEDONE.txt");
				BufferedWriter output = new BufferedWriter(new FileWriter(fileOut));
			    output.write(fileStr);
			    output.close();   */
			    
			    System.out.println("----------------- END ------------------");


		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
    
	public  static void send(String msg) throws JMSException, Exception
	{
	    //String  cfName                  = "jms/WLQueueConnectionFactory";
		String  cfName                  = "jms.conn.tdp.udq";  //A1

	    Session                session    = null;
	    Connection             connection = null;
	    ConnectionFactory      cf         = null;
	    MessageProducer        mp         = null;
	    Destination            destination = null;

	    try {

	    	Hashtable env = new Hashtable();
	    	env.put(Context.INITIAL_CONTEXT_FACTORY,
	    	     "weblogic.jndi.WLInitialContextFactory");
	    	env.put(Context.PROVIDER_URL, "t3://10.201.1.18:9301/");
	    	Context initialContext = new InitialContext(env);
	    	//logger.debug( "Getting Connection Factory");

			cf= (ConnectionFactory)initialContext.lookup( cfName );

			//logger.debug( "Getting Queue");
			destination =(Destination)initialContext.lookup("jms.distqueue.tdp.N4QueueInBT");

			//logger.debug( "Getting Connection for Queue");
			connection = cf.createConnection();

			//logger.debug( "staring the connection");
			connection.start();

			//logger.debug( "creating session");
			session = connection.createSession(false, 1);

			//logger.debug( "creating messageProducer");
			mp = session.createProducer(destination);

			//logger.debug( "creating TextMessage");
			TextMessage outMessage = session.createTextMessage( msg);

			System.out.println( "sending Message to queue: " + "jms.distqueue.tdp.N4QueueInBT");
			mp.send(outMessage);

			mp.close();
			session.close();
			connection.close();
	    }
	    catch (Exception je)
	    {
	    	je.printStackTrace();
	    }
	}

	
/*	public static void main( String[] args)
	{
		try
		{
			File file = new File("C:/A_INSTALL/UNIT_LOAD/UNIT_LOAD_write.txt");


			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line = null;
			String outfile = new String();
			StringBuffer strBuff = new StringBuffer();
			
			//out = new StringWriter();

		while ((line = br.readLine()) != null )
			{
				//String strOut = eventSpecificFieldValue(line,"aTime=","10:50:10");
				   sb.append(line + "\n");
			}

				br.close();
				//out.close();
				String fileStr = sb.toString();
				/*					//DcmConverterFileProcessor dcmConverter = new DcmConverterFileProcessor();
				//System.out.println("Before processFile(fileStr)");
				//dcmConverter.processFile(fileStr);
				//System.out.println("After processFile(fileStr)");
				
				File fileOut = new File("C:/A_INSTALL/UNIT_LOAD/UNIT_LOAD_Select.txt");
				BufferedWriter output = new BufferedWriter(new FileWriter(fileOut));
			    output.write(fileStr);
			    output.close();

		  String unitNbr = null;	
		  Iterator itr = getNbr(unitNbr).iterator();  
		   System.out.println("HashSet contains : ");    
			 
			   while(itr.hasNext()){
					String unitNb = (String)itr.next();   
			        if(!fileStr.contains(unitNb)){
			        	System.out.println("unitNb="+unitNb);    
			        }//IF
				   }
			   System.out.println(itr.next());  
		  
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}*/
	
	 public static String eventSpecificFieldValue(String xmlGvyData,String field,String newFieldValue)
	 {
	     String newValue = null;
	 	String tempNewValue = newFieldValue != null ? newFieldValue : "null"; //A2
	     String oldValue = null;
	     String xmlGvyString = xmlGvyData;
	     int fieldIndx = xmlGvyString.indexOf(field);
	     try
	    {
	        if(fieldIndx != -1)
	        {
	          int equalsIndx = xmlGvyString.indexOf("=",fieldIndx);
	          int nextspace = xmlGvyString.indexOf("'", equalsIndx+2);
	          oldValue = xmlGvyString.substring(equalsIndx+2, nextspace);
	 	  
	           if(oldValue.equals("null") ){
	             newValue = tempNewValue;
	           }
	           else{
	              //CHECK FOR VALUE HERE
	              newValue = tempNewValue;
	            }
	            //System.out.println("Field ::"+field+"  oldValue ::"+oldValue+"  newValue :::"+newValue);
	            //Replace Escape Char in String 
	            newValue = replaceQuotes(newValue);
	            String oldXmlValue = field+"'"+oldValue+"'";
	            String newXmlValue = field+"'"+newValue+"'";
	            xmlGvyString = xmlGvyString.replace(oldXmlValue, newXmlValue);
	         }//IF Ends
	      }catch(Exception e){
	          e.printStackTrace();
	      }
	      return xmlGvyString;
	    }// Method eventSpecificFieldValue Ends

	 
	 public static String replaceQuotes(Object message)
	   {
	     String msg = message.toString();
	     String replaceAmp = msg.replaceAll("&", "&amp;");
	           replaceAmp = replaceAmp.replaceAll("\'", "&apos;");
	           replaceAmp = replaceAmp.replaceAll("<", "&lt;");
	           replaceAmp =  replaceAmp.replaceAll(">", "&gt;");
	           replaceAmp = replaceAmp.replaceAll("\"", "&quot;");
	     return replaceAmp; 
	    }
	 
	 
	 
	 
	 public static HashSet getNbr(String nbr){
		HashSet map = new HashSet();
		map.add("MATU2313817");
		map.add("MATU2475675");
		map.add("MATU4549458");
		map.add("MATU5124231");
		map.add("MATU5141856");
		map.add("FSCU4064614");
		map.add("FSCU4486775");
		map.add("MATU5136058");
		map.add("MATU5135160");
		map.add("MATU3450795");
		map.add("MATU4526529");
		map.add("MATU5126337");
		map.add("MATU4553490");
		map.add("MATU4562423");
		map.add("TTNU5383071");
		map.add("MATU5116529");
		map.add("MATU5125367");
		map.add("MATU5140823");
		map.add("MATU5129850");
		map.add("MATU4575009");
		map.add("MATU4581444");
		map.add("MATU5136103");
		map.add("MATU5513064");
		map.add("MATU5139057");
		map.add("MATU4532820");
		map.add("MATU4555661");
		map.add("MATU4571277");
		map.add("MATU5130790");
		map.add("MATU4586317");
		map.add("MATU4526750");
		map.add("MATU3911644");
		map.add("MATU5130949");
		map.add("MATU5514517");
		map.add("MATU5127945");
		map.add("MATU4523350");
		map.add("CRXU9085291");
		map.add("MATU5135679");
		map.add("MATU4543260");
		map.add("MATU4575164");
		map.add("MATU4557263");
		map.add("MATU4541375");
		map.add("MATU5511483");
		map.add("MATU5121150");
		map.add("MATU4569011");
		map.add("MATU4558948");
		map.add("MATU5120405");
		map.add("MATU5122877");
		map.add("MATU5117802");
		map.add("MATU4544409");
		map.add("MATU4560035");
		map.add("MATU2487697");
		map.add("MATU5128920");
		map.add("MATU5124606");
		map.add("MATU5511668");
		map.add("MATU4567507");
		map.add("MATU4576197");
		map.add("MATU4545529");
		map.add("FSCU9633523");
		map.add("APHU6761472");
		map.add("MATU2266633");
		map.add("MATU5139611");
		map.add("MATU5134420");
		map.add("MATU2561623");
		map.add("MATU2485586");
		map.add("MATU5512406");
		map.add("MATU5129629");
		map.add("GEAR0000171");
		map.add("MATU2584866");
		map.add("MATU2493848");
		map.add("MATU5133402");
		map.add("MATU5116365");
		map.add("MATU5136931");
		map.add("MATU2303629");
		map.add("MATU5135750");
		map.add("MATU5129660");
		map.add("MATU5131713");
		map.add("CTDU5995050");
		map.add("MATU5119493");
		map.add("MATU5140253");
		map.add("MATU5139822");
		map.add("MATU5114737");
		map.add("MATU5124801");
		map.add("MATU5137917");
		map.add("MATU5140021");
		map.add("MWCU6271005");
		map.add("MATU2464623");
		map.add("MATU5132880");
		map.add("MATU5119024");
		map.add("MATU2580731");
		map.add("MATU5140994");
		map.add("MATU5118481");
		map.add("MATU5115733");
		map.add("MATU5137202");
		map.add("MATU5135869");
		map.add("MATU5123641");
		map.add("MATU2482695");
		map.add("MATU5513845");
		map.add("MATU5116982");
		map.add("MATU5133700");
		map.add("MATU5120133");
		map.add("MATU2478329");
		map.add("MATU5137389");
		map.add("MATU4529575");
		map.add("TGHU1807669");
		map.add("MATU2549850");
		map.add("MATU2529787");
		map.add("MATU2494799");
		map.add("MATU2526134");
		map.add("TRLU8277220");
		map.add("TCNU9726324");
		map.add("TGHU8801810");
		map.add("FSCU9649962");
		map.add("MATU2306593");
		map.add("MATU2272894");
		map.add("MATU2511324");
		map.add("MATU2579638");
		map.add("MATU2480727");
		map.add("TRLU5532981");
		map.add("CLHU4217892");
		map.add("MATU2534340");
		map.add("TCNU9827042");
		map.add("MATU2586072");
		map.add("FSCU6866706");
		map.add("MATU2264395");
		map.add("MATU2275363");
		map.add("MATU2560294");
		map.add("FSCU9812154");
		map.add("MATU2583598");
		map.add("CRXU4287026");
		map.add("MATU2315980");
		map.add("MATU2551461");
		map.add("MATU2575680");
		map.add("MATU5137033");
		map.add("MATU2544631");
		map.add("FSCU4194135");
		map.add("MATU2536554");
		map.add("MATU2481030");
		map.add("MATU2256126");
		map.add("MATU2582267");
		map.add("MATU2539358");
		map.add("MATU2312889");
		map.add("FSCU9642438");
		map.add("MATU2513374");
		map.add("MATU2582020");
		map.add("CAXU7154948");
		map.add("TTNU4557688");
		map.add("MATU2526601");
		map.add("MATU5121227");
		map.add("MATU2302556");
		map.add("MATU2308718");
		map.add("MATU2300492");
		map.add("HLXU4123026");
		map.add("MATU2478695");
		map.add("MATU2517240");
		map.add("MATU5134348");
		map.add("MATU4573583");
		map.add("MATU4556143");
		map.add("TTNU5836199");
		map.add("MATU2474529");
		map.add("MATU4527490");
		map.add("MATU4583771");
		map.add("TGHU8802083");
		map.add("MATU2301605");
		map.add("MATU2536805");
		map.add("MATU2567869");
		map.add("MATU4569618");
		map.add("MATU2312826");
		map.add("MATU4532286");
		map.add("MATU4573897");
		map.add("MATU2484770");
		map.add("MATU7004338");
		map.add("MATU2458410");
		map.add("MATU2565995");
		map.add("MATU4573644");
		map.add("MATU4560858");
		map.add("MATU2558511");
		map.add("MATU2266398");
		map.add("MATU4535330");
		map.add("MATU5126682");
		map.add("MATU5121377");
		map.add("TEXU7324736");
		map.add("TCLU4122453");
		map.add("MATU4530508");
		map.add("MATU4565464");
		map.add("MATU5123615");
		map.add("MATU2525776");
		map.add("FSCU9171636");
		map.add("MATU2274095");
		map.add("MATU4538730");
		map.add("MATU4528898");
		map.add("TCLU4122915");
		map.add("MATU2542177");
		map.add("MATU2467113");
		map.add("MATU2466035");
		map.add("MATU2550887");
		map.add("MATU4581974");
		map.add("MATU4531459");
		map.add("MATU5138390");
		map.add("MATU2563884");
		map.add("MATU2546167");
		map.add("MATU2522000");
		map.add("MATU4557010");
		map.add("MATU5133125");
		map.add("MATU5134667");
		map.add("MATU2552540");
		map.add("CRXU9146421");
		map.add("MATU4556821");
		map.add("MATU5127673");
		map.add("TTNU5483466");
		map.add("MATU2508830");
		map.add("MATU2277664");
		map.add("MATU2297130");
		map.add("TRLU4430922");
		map.add("TRLU7192638");
		map.add("MATU2536404");
		map.add("MATU2267877");
		map.add("MLCU4771864");
		map.add("MATU3701118");
		map.add("MATU2514344");
		map.add("TCLU8013614");
		map.add("MATU2544400");
		map.add("TTNU9739290");
		map.add("TGHU6153998");
		map.add("MATU2479243");
		map.add("MATU2519346");
		map.add("MATU2310886");
		map.add("TCLU4120790");
		map.add("MATU2315851");
		map.add("TEXU5391360");
		map.add("MATU2566923");
		map.add("MATU2307183");
		map.add("CAXU4699435");
		map.add("MATU2313695");
		map.add("MATU2293794");
		map.add("MATU2550758");
		map.add("MATU7004831");
		map.add("MATU7006942");
		map.add("MATU2309972");
		map.add("MATU5127550");
		map.add("MATU5125808");
		map.add("TRLU6551815");
		map.add("MATU2543996");
		map.add("MATU2534016");
		map.add("MATU2562580");
		map.add("MATU2470816");
		map.add("MATU5118877");
		map.add("MATU2503524");
		map.add("TCNU8632811");
		map.add("MATU2265750");
		map.add("MATU2570610");
		map.add("MATU5116658");
		map.add("MATU2280729");
		map.add("MATU2527336");
		map.add("MATU2549042");
		map.add("MATU2279625");
		map.add("MATU5138847");
		map.add("MATU7007358");
		map.add("TCKU9870050");
		map.add("TGHU7688799");
		map.add("MATU5128108");
		map.add("MATU2540699");
		map.add("MATU2541289");
		map.add("MSKU6287911");
		map.add("CPSU6205593");
		map.add("MATU2504794");
		map.add("MATU5140910");
		map.add("TGHU6156111");
		map.add("MATU2566585");
		map.add("MATU2555739");
		map.add("MATU5141563");
		map.add("MATU6875366");
		map.add("MATU6875999");
		map.add("MATU6876850");
		map.add("MATU6871165");
		map.add("MATU6872670");
		map.add("MATU6873445");
		map.add("MATU6876784");
		map.add("MATU6871801");
		map.add("MATU6876362");
		map.add("MATU2477512");
		map.add("TCLU8010847");
		map.add("MATU4561767");
		map.add("MATU2581276");
		map.add("MATU4565891");
		map.add("TRIU0819470");
		map.add("MATU2537519");
		map.add("FSCU6113810");
		map.add("MATU2507129");
		map.add("MATU2483768");
		map.add("MATU2519599");
		map.add("MATU2483388");
		map.add("FBLU9030919");
		map.add("MATU2506246");
		map.add("MATU4563543");
		map.add("MATU4542010");
		map.add("MATU2461455");
		map.add("MATU2539912");
		map.add("MATU2281509");
		map.add("MATU2304862");
		map.add("MATU4548683");
		map.add("MATU7005571");
		map.add("MATU2462997");
		map.add("TRLU8278612");
		map.add("MATU3903016");
		map.add("MATU2516434");
		map.add("MATU2291132");
		map.add("MATU5117253");
		map.add("TTNU4130037");
		map.add("MATU2473986");
		map.add("MATU2285377");
		map.add("MATU7001848");
		map.add("MATU2557562");
		map.add("MATU2541519");
		map.add("MATU2504583");
		map.add("MATU3912548");
		map.add("MATU2281880");
		map.add("MATU2584763");
		map.add("MATU2560083");
		map.add("SCZU4817156");
		map.add("MATU2553783");
		map.add("MATU2314198");
		map.add("TRLU6541437");
		map.add("MATU2548730");
		map.add("MATU2528100");
		map.add("MATU2264944");
		map.add("MATU2275830");
		map.add("MATU2490540");
		map.add("MATU2309397");
		map.add("TRLU6318948");
		map.add("MATU2295550");
		map.add("MATU2587150");
		map.add("MATU2570517");
		map.add("FSCU9783400");
		map.add("MATU2290497");
		map.add("MATU2501711");
		map.add("TRLU7040240");
		map.add("MATU2465804");
		map.add("TRLU7186928");
		map.add("TCLU8003004");
		map.add("TRLU8079098");
		map.add("FSCU6364647");
		map.add("MATU2481940");
		map.add("MATU2547625");
		map.add("MATU2533895");
		map.add("MATU2521132");
		map.add("MATU2535630");
		map.add("TCNU9172480");
		map.add("TCLU8006805");
		map.add("MATU2584254");
		map.add("MATU2505893");
		map.add("MATU2573429");
		map.add("MATU2547013");
		map.add("MATU2572680");
		map.add("MATU5120874");
		map.add("MATU2511704");
		map.add("MATU2460253");
		map.add("TRLU8078661");
		map.add("MATU2538156");
		map.add("MATU2515382");
		map.add("MATU2574528");
		map.add("TTNU5499122");
		map.add("TTNU4482064");
		map.add("MATU2587818");
		map.add("MATU2560653");
		map.add("MATU2300147");
		map.add("MATU2499548");
		map.add("TCLU8004464");
		map.add("TGHU4084291");
		map.add("MATU2505390");
		map.add("MATU6875880");
		map.add("MATU6873363");
		map.add("MATU6875089");
		map.add("MATU6870894");
		map.add("MATU6870240");
		map.add("MATU6874056");
		map.add("MATU6872346");
		map.add("MATU2073676");
		map.add("IDTU0241135");
		map.add("MATU2080253");
		map.add("SCZU7991726");
		map.add("IDTU0240226");
		map.add("DCIU8451539");
		map.add("TCLU2761265");
		map.add("GSTU5702160");
		map.add("TGHU1690715");
		map.add("HLXU3492082");
		map.add("DCIU8451842");
		map.add("MATU2072920");
		map.add("GSTU5912948");
		map.add("DCIU2140139");
		map.add("DCIU2140205");
		map.add("MATU4540002");
		map.add("MATU4541210");
		map.add("MATU2289602");
		map.add("EADE70381");
		map.add("MATU4574208");
		map.add("MATU4555846");
		map.add("MATU4564895");
		map.add("TRLU7039655");
		map.add("MATU4528310");
		map.add("MATU4564370");
		map.add("MATU4582245");
		map.add("EADE9000370");
		map.add("MATU4562700");
		map.add("MATU4577249");
		map.add("MATU4572669");
		map.add("MATU4575570");
		map.add("MATU4564679");
		map.add("MATU4519448");
		map.add("MATU2298749");
		map.add("MATU4559158");
		map.add("MATU2267394");
		map.add("TCLU8006554");
		map.add("MATU2532749");
		map.add("MATU4586702");
		map.add("MATU2496805");
		map.add("TCNU9536685");
		map.add("TEXU2245803");
		map.add("APZU3582835");
		map.add("TGHU1807139");
		map.add("MATU2083890");
		map.add("IDTU0240669");
		map.add("TGHU1802230");
		map.add("MATU2077060");
		map.add("MLCU2788092");
		map.add("TGHU1801742");
		map.add("MATU2086879");
		map.add("MATU2082425");
		map.add("TRLU3888187");
		map.add("IDTU0241639");
		map.add("IDTU0241984");
		map.add("IDTU0241520");
		map.add("TPTU2898756");
		map.add("IDTU0241290");
		map.add("MATU2089121");
		map.add("TGHU1801819");
		map.add("MATU4556518");
		map.add("MATU4531546");
		map.add("MATU4550567");
		map.add("MATU4524001");
		map.add("MATU4578029");
		map.add("MATU4546356");
		map.add("MATU4551516");
		map.add("MATU4518077");
		map.add("MATU4539212");
		map.add("MATU4532054");
		map.add("MATU4558228");
		map.add("MATU4528692");
		map.add("MATU4564128");
		map.add("MATU4577758");
		map.add("MATU4582800");
		map.add("MATU4546870");
		map.add("MATU4540363");
		map.add("TCNU9083320");
		map.add("MATU2517765");
		map.add("MATU4579323");
		map.add("MATU4574759");
		map.add("MATU2529283");
		map.add("MATU4562980");
		map.add("MATU4538860");
		map.add("MATU4533810");
		map.add("MATU3701864");
		map.add("MATU2554732");
		map.add("MATU4576860");
		map.add("MATU4584335");
		map.add("MATU2486670");
		map.add("MATU2581018");
		map.add("MATU4530154");
		map.add("MATU4547780");
		map.add("MATU4558090");
		map.add("FSCU9782954");
		map.add("FSCU9650650");
		map.add("MATU2578415");
		map.add("MATU4533889");
		map.add("MATU2505070");
		map.add("MATU2286579");
		map.add("MATU2559499");
		map.add("TRLU8077454");
		boolean isNbr = map.contains(nbr);
		return map;
		//return map;
	 }
}
