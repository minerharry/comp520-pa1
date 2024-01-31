package miniJava.SyntacticAnalyzer;

import miniJava.Compiler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Token {

	public static final HashSet<Character> opstarts = new HashSet<>(Arrays.asList('+','/','*','-','%','|','&','~','!', '=', '>','<')); //both unary and binary operators
	public static final HashSet<Character> dualOps = new HashSet<>(Arrays.asList('+','-')); //both unary and binary operators
	public static final HashSet<Character> assignOps = new HashSet<>(Arrays.asList('+','/','*','-','%','|','&')); //both unary and binary operators

	public static final HashMap<Character,TokenType> punctuation = new HashMap<>(){{
		this.put('.',TokenType.dot);
		this.put(',',TokenType.comma);
		this.put(':',TokenType.colon);
		this.put(';',TokenType.semicolon);
		this.put('?',TokenType.question);
		this.put('{',TokenType.lcurly);
		this.put('}',TokenType.rcurly);
		this.put('(',TokenType.lparen);
		this.put(')',TokenType.rparen);
		this.put('[',TokenType.lsquare);
		this.put(']',TokenType.rsquare);
	}};

	public static final HashMap<String,TokenType> keywords = new HashMap<>(){{
		this.put("class", TokenType.classKeyword);
		this.put("if",TokenType.ifKeyword);
		this.put("else",TokenType.elseKeyword);
		this.put("for", TokenType.forKeyword);
		this.put("this", TokenType.thisKeyword);
		this.put("while", TokenType.whileKeyword);
		this.put("void",TokenType.voidKeyword);
		this.put("new",TokenType.newKeyword);
		this.put("return",TokenType.returnKeyword);
		this.put("true",TokenType.boolLiteral);
		this.put("false",TokenType.boolLiteral);
		if (!Compiler.IS_MINI){
			this.put("package",TokenType.packageKeyword);
			this.put("import",TokenType.importKeyword);
			this.put("throws",TokenType.throwsKeyword);
			this.put("throw",TokenType.throwKeyword);
			this.put("implements",TokenType.implementsKeyword);
			this.put("extends",TokenType.extendsKeyword);
			this.put("try",TokenType.tryKeyword);
			this.put("catch",TokenType.catchKeyword);
			this.put("finally",TokenType.finallyKeyword);
			this.put("break",TokenType.breakKeyword);
			this.put("continue",TokenType.continueKeyword);
			this.put("do",TokenType.doKeyword);
			this.put("switch",TokenType.switchKeyword);
			this.put("case",TokenType.caseKeyword);
		}
	}};

	public static final HashMap<String,TokenType> primitives = new HashMap<>(){{
		this.put("int",TokenType.intPrimitive);
		this.put("boolean",TokenType.boolPrimitive);
		this.put("String",TokenType.stringPrimitive);
		this.put("float",TokenType.floatPrimitive);
		this.put("double",TokenType.doublePrimitive);
		this.put("char",TokenType.charPrimitive);
	}};

	public static final HashSet<TokenType> literals = new HashSet<>(){{
		this.add(TokenType.boolLiteral);
		this.add(TokenType.intLiteral);
		this.add(TokenType.charLiteral);
		this.add(TokenType.floatLiteral);
		this.add(TokenType.stringLiteral);
	}};

	public static final HashSet<String> protectionKeywords = new HashSet<>(Arrays.asList("public","private","protected"));
	public static final HashSet<String> modifierKeywords = new HashSet<>(Arrays.asList("static","final","volatile","abstract"));

	private TokenType _type;
	private String _text;
	private int _line;
	private int _col;
	
	public Token(TokenType type, String text, int lineno, int colno) {
		_type = type;
		_text = text;
		_line = lineno;
		_col = colno;
	}
	
	public TokenType getTokenType() {
		return _type;
	}
	
	public String getTokenText() {
		return _text;
	}

	public String toString(){
		return "Token of type {" + _type + "}: " + _text + " at line " + _line + ":" + _col;
	}
}
