
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

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
		
		void prependHeader(String what) {
			cppHeader = what + cppHeader;
		}
		void appendHeader(String what) {
			cppHeader = cppHeader + what;
		}
		void prependSource(String what) {
			cppSource = what + cppSource;
		}
		void appendSource(String what) {
			cppSource = cppSource + what;
		}
		
		SCQueryListener(BufferedTokenStream tokens) {
			this.tokens = tokens;			
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
//			if( null != ctx.blockdeclaration() ) {
//				CPP14Parser.BlockdeclarationContext bdc = ctx.blockdeclaration();
//				if( null != bdc.simpledeclaration() ) {
//					CPP14Parser.SimpledeclarationContext sd = bdc.simpledeclaration();
//					if( null != sd.declspecifierseq() ) {
//						CPP14Parser.DeclspecifierseqContext dss = sd.declspecifierseq();
//						if( null != dss.declspecifier() ) {
//							CPP14Parser.DeclspecifierContext ds = dss.declspecifier();
//							if( null != ds.typespecifier() ) {
//								CPP14Parser.TypespecifierContext ts = ds.typespecifier();
//								if( null != ts.classspecifier() ) {
//									// Found class declaration with a body, skip it, deal with it later
//									return;
//								}
//								
//							}
//						}
//					}
//				}
//			} else 
			
//			if( null != ctx.functiondefinition() ) {
//				CPP14Parser.FunctiondefinitionContext fd = ctx.functiondefinition();
//				// Skip function definitions with implementations
//				if( null != fd.functionbody() )
//					return;				
//			}
			
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
			System.out.println(ctx.start.getInputStream().getText(interval));

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
			System.out.println("GENERATED: " + classKey + className + classVirtSpecifier + baseClause + "{" );
		}
		
		@Override
		public void exitClassspecifier(CPP14Parser.ClassspecifierContext ctx) {
			System.out.println("GENERATED: " + "};");
		}
		
		@Override
		public void enterFunctiondefinition(CPP14Parser.FunctiondefinitionContext ctx) {
			System.out.println("GENERATED: " + ctx.getText());
		}
		
		@Override
		public void enterMemberdeclaration(CPP14Parser.MemberdeclarationContext ctx) {
			int a = ctx.start.getStartIndex();
			int b = ctx.stop.getStopIndex();
			Interval interval = new Interval(a, b);
			String declaration = ctx.start.getInputStream().getText(interval)+" ";
			System.out.println("GENERATED: " + declaration);
		}
		
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
