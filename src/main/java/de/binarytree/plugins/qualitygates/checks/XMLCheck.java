package de.binarytree.plugins.qualitygates.checks;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.util.FormValidation;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLCheck extends Check {

	private String expression;
	private String targetFile;

	@DataBoundConstructor
	public XMLCheck(String targetFile, String expression) {
		this.expression = expression;
		this.targetFile = targetFile;
	}

	public String getExpression() {
		return this.expression;
	}

	public String getTargetFile() {
		return this.targetFile;
	}

	@Override
	public Result doCheck(AbstractBuild build, BuildListener listener,
			Launcher launcher) {
		try {
			InputStream stream = obtainInputStream(build);
			if (this.xmlContainsXPathExpression(stream)) {
				return Result.SUCCESS;
			}
		} catch (Exception e) {
			return Result.FAILURE;
		}
		return Result.FAILURE;
	}

	@Override
	public String toString() {
		return super.toString() + "[Occurence of " + this.getExpression()
				+ " in " + this.getTargetFile() + "]";
	}

	protected InputStream obtainInputStream(AbstractBuild build)
			throws IOException {
		FilePath pom = build.getModuleRoot().child(this.targetFile);
		InputStream stream = pom.read();
		return stream;
	}

	public boolean xmlContainsXPathExpression(InputStream stream)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(false); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(stream);

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile(this.getExpression());

		Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		// System.out.println(nodes.item(i).getTextContent());
		return nodes.getLength() > 0;
	}

	@Extension
	public static class DescriptorImpl extends CheckDescriptor {

		@Override
		public String getDisplayName() {
			return "Check XML file for tags";
		}

		public FormValidation doCheckExpression(@QueryParameter String value) {
			if (value.length() == 0) {
				return FormValidation
						.error("XPath expression must not be empty");
			} else {
				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();
				try {
					XPathExpression expr = xpath.compile(value);
				} catch (XPathExpressionException e) {
					return FormValidation
							.error("XPath expression is not valid.");
				}
				return FormValidation.ok();
			}
		}

		public FormValidation doCheckTargetFile(@QueryParameter String value) {
			if (value.length() == 0) {
				return FormValidation.error("Target file must not be empty");
			} else if (value.contains("..")) {
				return FormValidation
						.error("Parent directory '..' may not be referenced");
			} else if (value.startsWith("/")) {
				return FormValidation.error("Path may not be absolute.");
			}
			return FormValidation.ok();
		}

	}
}