
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;


class SCQuery {

	public static class SCQueryListener extends CPP14BaseListener {
		
		BufferedTokenStream tokens;
		
		String cppHeader = "";
		String cppSource = "";
		
		Stack<String> currentClass;
		
		void prependHeader(String what) {
			cppHeader = what + cppHeader + "\n";
		}
		void appendHeader(String what) {
			cppHeader = cppHeader + what + "\n";
		}
		void prependSource(String what) {
			cppSource = what + cppSource + "\n";
		}
		void appendSource(String what) {
			cppSource = cppSource + what + "\n";
		}
		
		SCQueryListener(BufferedTokenStream tokens) {
			this.tokens = tokens;
			currentClass = new Stack<>();
		}
		
		@Override
		public void enterDeclaration(CPP14Parser.DeclarationContext ctx) {
			Token start = ctx.getStart();
			int i = start.getTokenIndex();
			List<Token> dirChannel = tokens.getHiddenTokensToLeft(i, 1); 
			if(dirChannel != null) {
				for( Token dir : dirChannel ) {
					if(dir != null) {
						String txt = dir.getText();
						System.out.println(txt);
					}
				}
			}
			
			// TODO : filter out class and explicit function declarations
			// TODO : keep templates, function prototypes, global variables, and pretty much everything else
			
			try {
				if( null != ctx.functiondefinition() )
					return;
				
			} catch (Throwable e) {}
			
			try { 
				if ( null != 
						ctx.blockdeclaration().
						simpledeclaration().
						declspecifierseq().
						declspecifier().
						typespecifier().
						classspecifier()
						)
					return;
			} catch (Throwable e) {}

			int a = ctx.start.getStartIndex();
			int b = ctx.stop.getStopIndex();
			Interval interval = new Interval(a, b);
			appendHeader(ctx.start.getInputStream().getText(interval));

		}
		
		@Override
		public void enterClassspecifier(CPP14Parser.ClassspecifierContext ctx) {
			String classKey = ctx.classhead().classkey().getText()+" ";
			String className = ctx.classhead().classheadname().getText()+" ";
			String classVirtSpecifier="";
			if(null != ctx.classhead().classvirtspecifier())
				classVirtSpecifier = ctx.classhead().classvirtspecifier().getText()+" ";
			String baseClause="";
			if(null != ctx.classhead().baseclause()) {
				int a = ctx.classhead().baseclause().start.getStartIndex();
				int b = ctx.classhead().baseclause().stop.getStopIndex();
				Interval interval = new Interval(a, b);
				baseClause= ctx.start.getInputStream().getText(interval)+" ";
			}
			appendHeader(classKey + className + classVirtSpecifier + baseClause + "{" );
		}
		
		@Override
		public void exitClassspecifier(CPP14Parser.ClassspecifierContext ctx) {
			appendHeader("};");
		}
		
		@Override
		public void enterFunctiondefinition(CPP14Parser.FunctiondefinitionContext ctx) {
			String prototypeString = "";
			int a,b;
			// Return type, access specifiers and other stuff
			if( null != ctx.declspecifierseq() ) {
				a = ctx.declspecifierseq().start.getStartIndex();
				b = ctx.declspecifierseq().stop.getStopIndex();
				prototypeString += ctx.start.getInputStream().getText(new Interval(a, b))+" ";
			}
			
			// Function name and arguments
			a=ctx.declarator().start.getStartIndex();
			b=ctx.declarator().stop.getStopIndex();
			prototypeString += ctx.start.getInputStream().getText(new Interval(a, b));
			
			prototypeString += ";";
			
			// Write to header
			appendHeader(prototypeString);
			
			// TODO : Write to source
		}
		
		@Override
		public void enterMemberdeclaration(CPP14Parser.MemberdeclarationContext ctx) {
			int a = ctx.start.getStartIndex();
			int b = ctx.stop.getStopIndex();
			Interval interval = new Interval(a, b);
			String declaration = ctx.start.getInputStream().getText(interval)+" ";
			
			boolean isField = false;
			try {
				isField = null != ctx.memberdeclaratorlist(); 
			} catch (Throwable e) {}

			boolean isPrototype = false;
			try {
				isPrototype = null == ctx.functiondefinition().functionbody(); 
			} catch (Throwable e) {}
			
			if(isField || isPrototype)
				appendHeader(declaration);
		}
		
		// TODO treat static fields
		
	}

	public static void main(String[] args) throws IOException {
		CPP14Lexer lexer = new CPP14Lexer(new ANTLRInputStream(new FileInputStream("example.cpp")));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CPP14Parser parser = new CPP14Parser(tokens);
		SCQueryListener listener = new SCQueryListener(tokens);
		CPP14Parser.TranslationunitContext context = parser.translationunit();

		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(listener, context);
		
		System.out.println("*********Header:");
		System.out.println(listener.cppHeader);
		System.out.println("*********Source:");
		System.out.println(listener.cppSource);
		
	}

}
