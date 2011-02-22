package featureide.fm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import featureide.fm.eval.Evaluation;
import featureide.fm.eval.EvaluationReport;

public class XStreamUtil {

	public static void save(EvaluationReport report) {
		File dir = new File(Evaluation.rootPath + EvaluationReport.dirPath
				+ report.getSpec().getReportDir());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		XStream stream = new XStream();

		try {
			String filename = report.getName() + ".xml";
			FileOutputStream fs = new FileOutputStream(new File(dir, filename));
			stream.toXML(report, fs);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static EvaluationReport load(String reportName) {
		EvaluationReport report = new EvaluationReport();

		File dir = new File(Evaluation.rootPath + EvaluationReport.dirPath
				+ report.getSpec().getReportDir());
		if (!dir.exists())
			return report;

		XStream xs = new XStream(new DomDriver());

		try {
			FileInputStream fis = new FileInputStream(new File(dir, reportName
					+ ".xml"));
			xs.fromXML(fis, report);

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}

		return report;
	}

	public static EvaluationReport load(File file) {
		EvaluationReport report = new EvaluationReport();
		XStream xs = new XStream(new DomDriver());

		try {
			FileInputStream fis = new FileInputStream(file);
			xs.fromXML(fis, report);

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}

		return report;
	}

}
