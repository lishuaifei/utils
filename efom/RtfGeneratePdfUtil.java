/**
 *  Copyright (c)  2016-2020 CCS, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of CCS, 
 *  Inc. ("Confidential Information"). You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into with CCS.
 */
package com.ccs.core.util.efom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import oracle.apps.xdo.template.FOProcessor;

import com.ccs.core.util.RtfTemplateUtil;
import com.ccs.core.util.StringUtil;
import com.thoughtworks.xstream.XStream;
/**
 * rtf templates generate PDF files
 *
 * @author sfli
 * @date Mar 12, 2017
 */
public class RtfGeneratePdfUtil {

	/**
	 * 将bean通过模板生成pdf
	 * @param valueMap 实体
	 * @param templatePath 模板位置
	 * @param fontPath 字体位置
	 * @param targetFilePath 生成的文件位置
	 * @throws Exception
	 */
	public static File createPdf(String templatePath, Object valueMap, String fontPath,
								 String targetFilePath) throws Exception {
		XStream xstream = new XStream();
		String xdoPath = System.getProperty("web.root") + "xdo.cfg";

		if(StringUtil.isEmpty(fontPath)){
			fontPath = System.getProperty("web.root") +"fonts";
			File xdo = new File(xdoPath);
			if(!xdo.exists()){
				RtfTemplateUtil.generateXdoCfg(xdoPath, fontPath);
			}
		}else{
			RtfTemplateUtil.generateXdoCfg(xdoPath, fontPath);
		}
		//去掉包名
		xstream.alias(valueMap.getClass().getSimpleName(), valueMap.getClass());

		String hlStr = xstream.toXML(valueMap);
		String outStr = "<?xml version=\"1.0\" encoding= \"UTF-8\" standalone=\"yes\"?>\r\n";
		outStr += hlStr;

		//公共路径
		String commonPath = targetFilePath.substring(0,targetFilePath.lastIndexOf(File.separator)+1);
		//生成的文件名称
		String fileName = targetFilePath.substring(targetFilePath.lastIndexOf(File.separator)+1,targetFilePath.length());
		//公共文件名
		String commonFileName = fileName.substring(0, fileName.lastIndexOf("."));
		//生成的xml名称
		String xmlName = commonPath+commonFileName+".xml";

		//写入到文件,生成xml文件
		FileWriter fw = null;
		File f = new File(xmlName);
		try{
			//判断是否存在
			if(!f.exists()) {
				f.createNewFile();
			}
			fw = new FileWriter(f);
			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
			BufferedWriter out=new BufferedWriter(write);
			out.write(outStr, 0, outStr.length());
			out.close();
			write.close();
		}catch (IOException e) {
			e.printStackTrace();
		}

		RtfTemplateUtil rtfUtil = new RtfTemplateUtil(
				templatePath,//rtf模板名称路径
				commonPath+commonFileName+".xsl", //中间过程生成的xsl路径
				xmlName, // xml数据文件路径
				xdoPath,//配置文件位路径
				targetFilePath,//输出最终文件路径
				FOProcessor.FORMAT_PDF
		);

		return rtfUtil.genarateReportPdf();
	}
	/**
	 * 根据模板生成PDF
	 * @param templatePath 模板位置
	 * @param valueMap 值
	 * @param fontPath 字体位置
	 * @param targetFilePath 生成文件的位置
	 * @return
	 * @throws Exception
	 */
	public static File createPdf(String templatePath, Object valueMap,String tagName) throws Exception {
		String tagpath = System.getProperty("web.root")+"temp"+File.separator +tagName+".pdf";
		return createPdf(templatePath,valueMap,"",tagpath);
	}

}