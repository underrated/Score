package org.textforest.score;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

class Score {

	public static class ScoreListener extends CPP14BaseListener {

		BufferedTokenStream tokens;

		String cppHeader = "";
		String cppSource = "";

		Stack<ParserRuleContext> currentContext;

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

		ScoreListener(BufferedTokenStream tokens) {
			this.tokens = tokens;
			currentContext = new Stack<>();
		}

		boolean isTemplate(ParserRuleContext ctx) {
			if (null == ctx)
				return false;
			boolean result = false;
			if (ctx instanceof CPP14Parser.ClassspecifierContext) {
				CPP14Parser.TypespecifierContext tspec = (ctx.getParent() instanceof CPP14Parser.TypespecifierContext)
						? (CPP14Parser.TypespecifierContext) ctx.getParent() : null;
				CPP14Parser.DeclspecifierContext dspec = (null != tspec
						&& tspec.getParent() instanceof CPP14Parser.DeclspecifierContext)
								? (CPP14Parser.DeclspecifierContext) tspec.getParent() : null;
				CPP14Parser.DeclspecifierseqContext dspecseq = (null != dspec
						&& dspec.getParent() instanceof CPP14Parser.DeclspecifierseqContext)
								? (CPP14Parser.DeclspecifierseqContext) dspec.getParent() : null;
				CPP14Parser.SimpledeclarationContext sdecl = (null != dspecseq
						&& dspecseq.getParent() instanceof CPP14Parser.SimpledeclarationContext)
								? (CPP14Parser.SimpledeclarationContext) dspecseq.getParent() : null;
				CPP14Parser.BlockdeclarationContext blkdecl = (null != sdecl
						&& sdecl.getParent() instanceof CPP14Parser.BlockdeclarationContext)
								? (CPP14Parser.BlockdeclarationContext) sdecl.getParent() : null;
				CPP14Parser.DeclarationContext decl = (null != blkdecl
						&& blkdecl.getParent() instanceof CPP14Parser.DeclarationContext)
								? (CPP14Parser.DeclarationContext) blkdecl.getParent() : null;
				result = null != decl ? decl.getParent() instanceof CPP14Parser.TemplatedeclarationContext : false;
			}

			if (ctx instanceof CPP14Parser.FunctiondefinitionContext) {
				CPP14Parser.DeclarationContext decl = (null != ctx
						&& ctx.getParent() instanceof CPP14Parser.DeclarationContext)
								? (CPP14Parser.DeclarationContext) ctx.getParent() : null;
				result = null != decl ? decl.getParent() instanceof CPP14Parser.TemplatedeclarationContext : false;
			}

			if (ctx instanceof CPP14Parser.MemberdeclarationContext) {
				CPP14Parser.MemberspecificationContext mspec = (null != ctx
						&& ctx.getParent() instanceof CPP14Parser.MemberspecificationContext)
								? (CPP14Parser.MemberspecificationContext) ctx.getParent() : null;
				
				ParserRuleContext ictx = mspec.getParent();
				while(ictx instanceof CPP14Parser.MemberspecificationContext)
					ictx = ictx.getParent();
				
				result = null != ictx && ictx instanceof CPP14Parser.ClassspecifierContext ? isTemplate(ictx) : false;
			}

			return result;
		}

		@Override
		public void enterDeclaration(CPP14Parser.DeclarationContext ctx) {

			// Keep the preprocessor directives wherever they may be
			Token start = ctx.getStart();
			int i = start.getTokenIndex();
			List<Token> dirChannel = tokens.getHiddenTokensToLeft(i, 1);
			if (dirChannel != null) {
				for (Token dir : dirChannel) {
					if (dir != null) {
						String txt = dir.getText();
						appendHeader(txt);
					}
				}
			}

			// Filter out class and explicit function declarations
			try {
				if (null != ctx.functiondefinition())
					return;

			} catch (Throwable e) {
			}

			try {
				if (null != ctx.blockdeclaration().simpledeclaration().declspecifierseq().declspecifier()
						.typespecifier().classspecifier())
					return;
			} catch (Throwable e) {
			}

			// TODO : keep templates, function prototypes, global variables, and
			// pretty much everything else

			int a = ctx.start.getStartIndex();
			int b = ctx.stop.getStopIndex();
			Interval interval = new Interval(a, b);
			appendHeader(ctx.start.getInputStream().getText(interval));

		}

		@Override
		public void enterClassspecifier(CPP14Parser.ClassspecifierContext ctx) {

			if (isTemplate(ctx))
				return;
			
			currentContext.push(ctx);

			String classKey = ctx.classhead().classkey().getText() + " ";
			String className = ctx.classhead().classheadname().getText() + " ";
			String classVirtSpecifier = "";
			if (null != ctx.classhead().classvirtspecifier())
				classVirtSpecifier = ctx.classhead().classvirtspecifier().getText() + " ";
			String baseClause = "";
			if (null != ctx.classhead().baseclause()) {
				int a = ctx.classhead().baseclause().start.getStartIndex();
				int b = ctx.classhead().baseclause().stop.getStopIndex();
				Interval interval = new Interval(a, b);
				baseClause = ctx.start.getInputStream().getText(interval) + " ";
			}
			appendHeader(classKey + className + classVirtSpecifier + baseClause + "{");
		}

		@Override
		public void exitClassspecifier(CPP14Parser.ClassspecifierContext ctx) {
			if(isTemplate(ctx)) return;
			appendHeader("};");
			currentContext.pop();
		}

		@Override
		public void enterFunctiondefinition(CPP14Parser.FunctiondefinitionContext ctx) {

			if (isTemplate(ctx))
				return;
			
			if(isTemplate(ctx.getParent()))
				return;

			int a, b;
			// Return type, access specifiers and other stuff
			String specSeq = "";
			if (null != ctx.declspecifierseq()) {
				a = ctx.declspecifierseq().start.getStartIndex();
				b = ctx.declspecifierseq().stop.getStopIndex();
				specSeq = ctx.start.getInputStream().getText(new Interval(a, b));
			}

			// Function name and arguments
			String funcNameArgs = "";
			a = ctx.declarator().start.getStartIndex();
			b = ctx.declarator().stop.getStopIndex();
			funcNameArgs = ctx.start.getInputStream().getText(new Interval(a, b));

			// Write prototype to header
			appendHeader(specSeq + " " + funcNameArgs + ";");

			// TODO : Write to source
			if( null != ctx.functionbody() ) {
					String defString = "";
					String funcBody="";
					a = ctx.functionbody().start.getStartIndex();
					b = ctx.functionbody().stop.getStopIndex();
					funcBody = ctx.start.getInputStream().getText(new Interval(a, b));
					if( !currentContext.isEmpty() ) {
						ParserRuleContext csx = (CPP14Parser.ClassspecifierContext)currentContext.peek();
						CPP14Parser.ClassspecifierContext cs = (csx instanceof CPP14Parser.ClassspecifierContext) ? (CPP14Parser.ClassspecifierContext)csx : null;
						if( null != cs ) {
							String className = cs.classhead().classheadname().getText();
							defString = specSeq + " " + className + "::" + funcNameArgs + " " + funcBody;
						}
					}
					else 
						defString = specSeq + " " + funcNameArgs + " " + funcBody;
					// Write implementation to source file
					appendSource(defString);				
			}
		}

		@Override
		public void exitFunctiondefinition(CPP14Parser.FunctiondefinitionContext ctx) {
		}

		@Override
		public void enterMemberdeclaration(CPP14Parser.MemberdeclarationContext ctx) {
			
			if( isTemplate(ctx) ) return;

			int a = ctx.start.getStartIndex();
			int b = ctx.stop.getStopIndex();
			Interval interval = new Interval(a, b);
			String declaration = ctx.start.getInputStream().getText(interval) + " ";

			boolean isField = false;
			try {
				isField = null != ctx.memberdeclaratorlist();
			} catch (Throwable e) {
			}

			boolean isPrototype = false;
			try {
				isPrototype = null == ctx.functiondefinition().functionbody();
			} catch (Throwable e) {
			}

			if (isField || isPrototype)
				appendHeader(declaration);
		}

		// TODO : treat static fields
		// TODO : 

	}

	public static void main(String[] args) throws IOException {
		CPP14Lexer lexer = new CPP14Lexer(new ANTLRInputStream(new FileInputStream("example.cpp")));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CPP14Parser parser = new CPP14Parser(tokens);
		ScoreListener listener = new ScoreListener(tokens);
		CPP14Parser.TranslationunitContext context = parser.translationunit();

		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(listener, context);

		System.out.println("*********Header:");
		System.out.println(listener.cppHeader);
		System.out.println("*********Source:");
		System.out.println(listener.cppSource);

	}

}
