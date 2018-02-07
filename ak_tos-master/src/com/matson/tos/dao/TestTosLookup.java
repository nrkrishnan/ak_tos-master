package com.matson.tos.dao;

import com.matson.cas.refdata.mapping.TosVgxMessageMt;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;

import javax.naming.Context;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;

public class TestTosLookup {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Connection con = getConnection(false);
		TosLookup lookup = new TosLookup(con);
		TosVgxMessageMt data = VgxDAO.getTosVgxMessageMt("MATU246909", "1401007");
//		List<TosVgxMessageMt> VGXs = vgxCriteria.list();

//		Map <String, String>result = lookup.getUnitCarrierDetailsForFacility("MATU246909", "1401007");
		if (data != null) {
			System.out.println("got value : "+data);
		} else {
			System.out.println("null : ");

		}


		/*System.out.println(lookup.getConsignee("1291418", null));
		System.out.println(lookup.getShipper("1291418", null));
		System.out.println(lookup.getConsignee("junk", "AAFES"));
		System.out.println(lookup.getShipper(null, "AAFES"));
		System.out.println(lookup.getConsignee("1291418", "AAFES"));
		System.out.println(lookup.getShipper("15118224", "AAFES"));*/
	}

	 public static Connection getConnection(boolean autoCommit) throws Exception {
	        String driverName = "oracle.jdbc.driver.OracleDriver";
	        Class.forName(driverName);
	        String url = "jdbc:oracle:thin:@10.101.3.161:1521:MATD03";
	        String user = "TOSMGR";
	        String pass = "tosmgr_tosd01";
	        Connection conn = DriverManager.getConnection(url,user,pass);
	        conn.setAutoCommit(autoCommit);
	        return conn;
	        
	    }
}
