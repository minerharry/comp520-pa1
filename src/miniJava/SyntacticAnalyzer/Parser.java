package miniJava.SyntacticAnalyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;


import miniJava.Compiler;
import miniJava.CompilerError;
import miniJava.ErrorReporter;

public class Parser {
	private Scanner _scanner;
	private ErrorReporter _errors;
	private Token _currentToken;
	
	public Parser( Scanner scanner, ErrorReporter errors ) {
		this._scanner = scanner;
		this._errors = errors;
		this._currentToken = this._scanner.scan();
	}
	
	class SyntaxError extends CompilerError {
		public SyntaxError(String message) {
			super(message);
		}

		public SyntaxError(String message, String file, int line) {
			super(message, file, line);
			//TODO Auto-generated constructor stub
		}

		private static final long serialVersionUID = -6461942006097999362L;
	}
	
	public void parse() {
		try {
			// The first thing we need to parse is the Program symbol
			parseProgram();
		} catch( SyntaxError e ) { }
	}
	
	// Program ::= (ClassDeclaration)* eot
	private void parseProgram() throws SyntaxError {
		parseHeader();
		while (this._currentToken.getTokenType() != TokenType.EOT){
			parseClassDeclaration();
		}
	}

	private void parseHeader() throws SyntaxError {
		if (Compiler.IS_MINI){
			return;
		}
		//parse package statement
		accept(TokenType.packageKeyword);
		accept(TokenType.id);
		while (acceptOptional(TokenType.dot)){
			accept(TokenType.id);
		}
		accept(TokenType.semicolon);
		
		//parse import statements
		while (acceptOptional(TokenType.importKeyword)){
			accept(TokenType.id);
			while (acceptOptional(TokenType.dot)){
				if (_currentToken.getTokenText() == "*"){
					accept(TokenType.binOp);
					//import a.b.c.*
					break;
				}
				accept(TokenType.id);
			}
			accept(TokenType.semicolon);
		}
	}
	
	// ClassDeclaration ::= class identifier { (FieldDeclaration|MethodDeclaration)* }
	private void parseClassDeclaration() throws SyntaxError {
		// TODO: Take in a "class" token (check by the TokenType)
		//  What should be done if the first token isn't "class"?
		if (!Compiler.IS_MINI){
			acceptOptional(TokenType.protection);
			while (acceptOptional(TokenType.modifier)){}
		}
		accept(TokenType.classKeyword,TokenType.id);
		if (!Compiler.IS_MINI && acceptOptional(TokenType.extendsKeyword)){
			parseType(false);
		}
		accept(TokenType.lcurly);
		
		while (!acceptOptional(TokenType.rcurly)){
			parseFieldOrMethod();
		}
	}


	int FIELD_OR_METHOD = 0;
	int FIELD = 1;
	int METHOD = 2;
	// [protection] [modifier] Type Identifier (FieldBody | MethodBody)
	private void parseFieldOrMethod() throws SyntaxError {
		//protection, modifier
		acceptOptional(TokenType.protection);
		while (acceptOptional(TokenType.modifier)){}
		//type
		int type = FIELD_OR_METHOD;
		if (acceptOptional(TokenType.voidKeyword)){
			type = METHOD;
		} else {
			parseType();
		}

		//name(s)
		if (acceptListOf(TokenType.id) > 1){
			if (type == METHOD){
				err(new SyntaxError("void keyword not allowed in field declaration"));
			}
			type = FIELD;
		}

		if (acceptOptional(TokenType.lparen)){
			if (type == FIELD){
				err(new SyntaxError("Unexpected token '(' in field declaration"));
			}
			if (!acceptOptional(TokenType.rparen))
			{
				do {
					parseType();
					if (!Compiler.IS_MINI) acceptOptional(TokenType.ellipsis);
					accept(TokenType.id);
				} while (acceptOptional(TokenType.comma));
				accept(TokenType.rparen);
			}
			if (acceptOptional(TokenType.throwsKeyword)){
				if (type == FIELD){
					err(new SyntaxError("Keyword 'throws' is not allowed in field declaration"));
				}
				type = METHOD;
				parseType(false); //method throws exceptions
			}	
			parseStatement(true,true,false);
			return;
		}
		if (acceptOptional(TokenType.assignment)){
			if (Compiler.IS_MINI){
				err(new SyntaxError("Field assignment on declaration not supported in minijava."));
			}
			if (type == METHOD){
				err(new SyntaxError("void keyword not allowed in field declaration"));
			}
			parseExpression();
			accept(TokenType.semicolon);
			return;
		}
		if (acceptOptional(TokenType.semicolon)){
			if (type == METHOD){
				err(new SyntaxError("void keyword not allowed in field declaration"));
			}
			return;
		}
		err(new SyntaxError("Unexpected token in class body: " + _currentToken));
	}


	private boolean parseStatement(){
		return parseStatement(false,true,false);
	}

	//block = require {Statement*}
	//foreach = allow Type id : reference;  if foreach, returns whether the statement is a valid foreach statement
	private boolean parseStatement(boolean block, boolean allow_keyword, boolean foreach){
		if (block || _currentToken.getTokenType() == TokenType.lcurly){
			accept(TokenType.lcurly);
			while (!acceptOptional(TokenType.rcurly)){
				parseStatement(); //foreach does not nest
			}
			return false;
		}
		Token t = _currentToken;
		if (acceptOptional(TokenType.returnKeyword)){
			if (!acceptOptional(TokenType.semicolon)){
				parseExpression();
				accept(TokenType.semicolon);
			}
		} else if (allow_keyword && !Compiler.IS_MINI && acceptOptional(TokenType.throwKeyword)){
			parseExpression();
			accept(TokenType.semicolon);
		} else if (allow_keyword && !Compiler.IS_MINI && (acceptAnyOptional(TokenType.breakKeyword,TokenType.continueKeyword))){
			accept(TokenType.semicolon);
		} else if (allow_keyword && !Compiler.IS_MINI && acceptOptional(TokenType.tryKeyword)) {
			parseStatement(true,false,false);
			boolean do_finally = false;
			if (acceptOptional(TokenType.catchKeyword)){
				accept(TokenType.lparen);
				parseType(false);
				accept(TokenType.id);
				accept(TokenType.rparen);
				parseStatement(true,false,false);
				do_finally = acceptOptional(TokenType.finallyKeyword);
			} else {
				do_finally = true;
				accept(TokenType.finallyKeyword);
			}
			if (do_finally){
				parseStatement(true,false,false);
			}
		} else if (allow_keyword && acceptOptional(TokenType.ifKeyword)){
			accept(TokenType.lparen);
			parseExpression();
			accept(TokenType.rparen);
			parseStatement();
			if (acceptOptional(TokenType.elseKeyword)){
				parseStatement();
			}
		} else if (allow_keyword && acceptOptional(TokenType.forKeyword)){
			//not doing for-each, interacts strangely with parsestatement
			accept(TokenType.lparen);
			if (!parseStatement(false,false,true)){ //returning true means parsed foreach statement
				accept(TokenType.semicolon);
				parseStatement(false,false,false);
				accept(TokenType.semicolon);
				parseStatement(false,false,false);
				acceptOptional(TokenType.semicolon);
			}
			accept(TokenType.rparen);
			parseStatement();
		} else if (allow_keyword && !Compiler.IS_MINI && acceptOptional(TokenType.doKeyword)){
			//do-while
			parseStatement();
			accept(TokenType.whileKeyword,TokenType.lparen);
			parseExpression();
			accept(TokenType.rparen);
			accept(TokenType.semicolon);
		} else if (allow_keyword && acceptOptional(TokenType.whileKeyword)){
			accept(TokenType.lparen);
			parseExpression();
			accept(TokenType.rparen);
			parseStatement();
		} else if (acceptOptional(TokenType.incOp)){ //prefix unop to increment/decrement reference
			int ttype = parseTypeOrReference();
			if (ttype == TYPE){
				err(new SyntaxError("Operator " + t +" not valid for Type;"));
			}
			accept(TokenType.semicolon);
		} else {
			int ttype = parseTypeOrReference();
			t = _currentToken;
			if (acceptOptional(TokenType.id)){
				if (ttype != TYPE && ttype != TYPE_OR_REFERENCE){
					err(new SyntaxError("Unexpected token after reference: " + t));
				}
				ttype = TYPE; //received Type id
				if (foreach && acceptOptional(TokenType.colon)){
					parseExpression();
					return true;
				}
				if (Compiler.IS_MINI || !acceptOptional(TokenType.semicolon)){ //if mini, always require assignment
					accept(TokenType.assignment);
					parseExpression(true);
					accept(TokenType.semicolon);
				}
			} else if (acceptOptional(TokenType.assignOp) || acceptOptional(TokenType.assignment) || acceptOptional(TokenType.incOp)){
				if (ttype == TYPE){
					err(new SyntaxError("Type missing identifier; unexpected token " + t));
				}
				if (ttype == REFERENCE_UNASSIGNABLE){ //unassignable
					err(new SyntaxError("Cannot assign to read only expression;"));
				}
				if (t.getTokenType() != TokenType.incOp){ // no expression to assign for ++,--
					parseExpression();
				}
				accept(TokenType.semicolon);
			} else if (acceptOptional(TokenType.semicolon)) {
				if (ttype != REFERENCE_UNASSIGNABLE){
					err(new SyntaxError("Reference value unused (" + _currentToken + ")"));
				}
			} else {
				err(new SyntaxError("Unexpected token after type/reference: " + t));
			}
		}
		return false;
	}

	private void parseExpression(){
		parseExpression(false);
	}

	private void parseExpression(boolean allow_array_literal){
		if (acceptOptional(TokenType.unOp) || acceptOptional(TokenType.genOp) || acceptOptional(TokenType.incOp)){ //looking for unops, priority over binop
			parseExpression();
		} else if (allow_array_literal && acceptOptional(TokenType.lcurly)) {
			do {
				if (_currentToken.getTokenType() == TokenType.rcurly) break;
				parseExpression();
			} while (acceptOptional(TokenType.comma));
			accept(TokenType.rcurly);
		} else if (acceptAnyOptional(Token.literals)){ //any literal is an expression
		} else if (acceptOptional(TokenType.lparen)){
			parseExpression();
			accept(TokenType.rparen);
		} else if (acceptOptional(TokenType.newKeyword)){
			parseType(false); //raw type without brackets
			if (acceptOptional(TokenType.lparen)){
				if (Compiler.IS_MINI){
					accept(TokenType.rparen);
				}
				else if (!acceptOptional(TokenType.rparen)){
					do {
						if (_currentToken.getTokenType() == TokenType.rparen) break;
						parseExpression();
					} while (acceptOptional(TokenType.comma));
					accept(TokenType.rparen);
				}
			}
			while (acceptOptional(TokenType.lsquare)){
				parseExpression();
				accept(TokenType.rsquare);
			}
		} else {
			int ttype = parseTypeOrReference();
			if (ttype == TYPE){
				err(new SyntaxError("Type is not a valid expression"));
			}
			//otherwise, a reference; valid expression
		}
		if (acceptAnyOptional(TokenType.incOp)){
			return;
		}
		if (acceptAnyOptional(TokenType.binOp,TokenType.compOp,TokenType.genOp,TokenType.lchevron,TokenType.rchevron)){
			parseExpression();
			return;
		}
		if (acceptOptional(TokenType.question)){ //ternary operator
			parseExpression();
			accept(TokenType.colon);
			parseExpression();
			return;
		}

	}


	//NOTE: DIFFERENT SYNTAX THAN ORIGINAL GRAMMAR - reference **includes the contents of any brackets and parentheses**; anything that could be assigned to.
	private static final int TYPE = 1;
	private static final int REFERENCE = 2;
	private static final int REFERENCE_UNASSIGNABLE = 3;
	private static final int TYPE_OR_REFERENCE = 0;
	private int parseTypeOrReference() throws SyntaxError{
		Token _base_token = _currentToken;
		if (acceptAnyOptional(Token.primitives.values())){
			if (Compiler.IS_MINI && _base_token.getTokenType() == TokenType.boolPrimitive){
				return TYPE; //don't allow boolean arrays because minijava is silly
			}
			while (acceptOptional(TokenType.lsquare)){
				accept(TokenType.rsquare);
			}
			return TYPE;
		}
		int type = TYPE_OR_REFERENCE; //start ambiguous
		acceptAny(TokenType.id,TokenType.thisKeyword);

		boolean last_assignable = false;
		boolean last_id = true;
		while (true) {
			if (acceptOptional(TokenType.dot)){
				accept(TokenType.id);
				last_assignable = true;
				last_id = true;
				continue;
			}
			if (acceptOptional(TokenType.lsquare)){
				Token c = _currentToken;
				if (!acceptOptional(TokenType.rsquare)){
					if (type == TYPE){
						//uh-oh, two overconstraints
						err(new SyntaxError("Invalid type or reference; Types cannot evaluate expressions in brackets. Expected ']', received " + c));
					}
					type = REFERENCE;
					parseExpression();
					accept(TokenType.rsquare);
					if (Compiler.IS_MINI){
						return REFERENCE; //can't have more than one level of depth in miniJava;
					}
				} else {
					if (type == REFERENCE){
						//uh-oh two overconstraints
						err(new SyntaxError("Invalid type or reference; Expected expression after '['', found " + c));
					}
					while (acceptOptional(TokenType.lsquare)){
						accept(TokenType.rsquare);
					}
					return TYPE;
				}
				last_assignable = true;
				last_id = false;
				continue;
			}
			if (!Compiler.IS_MINI && acceptOptional(TokenType.lchevron)){
				if (type == REFERENCE){ 
					err(new SyntaxError("Generics syntax not allowed in reference, only in type"));
				}
				type = TYPE;
				parseType();
				accept(TokenType.rchevron);
				last_assignable = false;
				last_id = false; //pretty sure you can't call id<type>()
				continue;
			}
			if (acceptOptional(TokenType.lparen)){
				if (type == TYPE){
					//uh-oh two overconstraints
					err(new SyntaxError("Invalid type or reference; cannot call method in type declaration"));
				}
				if (last_id == false){
					err(new SyntaxError("Unexpected Token '('; only method identifiers can be called as functions"));
				}
				type = REFERENCE;
				if (!acceptOptional(TokenType.rparen)){
					do {
						if (_currentToken.getTokenType() == TokenType.rparen) break; //allow trailing comma
						parseExpression();
					} while (acceptOptional(TokenType.comma));
					accept(TokenType.rparen);
				}
				if (Compiler.IS_MINI){
					return REFERENCE_UNASSIGNABLE;
				}
				last_assignable = false;
				last_id = false;
				continue;
			}
			break;
		}
		if (!last_assignable && type == REFERENCE){
			return REFERENCE_UNASSIGNABLE;
		}
		return type;		
	}



	private void parseType(){
		parseType(true);
	}

	private void parseType(boolean allow_array){
		boolean isPrimitive = false;
		Token _base_token = _currentToken;
		if (acceptAnyOptional(Token.primitives.values())){
			isPrimitive = true;
		} else {
			accept(TokenType.id);
		}
		if (!isPrimitive){
			while (true){
				if (acceptOptional(TokenType.dot)){
					accept(TokenType.id);
					continue;
				}
				if (!Compiler.IS_MINI && acceptOptional(TokenType.lchevron)){
					if (!acceptOptional(TokenType.rchevron)) {// allow for anonymous generics
						parseType();
						accept(TokenType.rchevron);
					}
					continue;
				}
				break;
			}
		}

		if (isPrimitive && miniJava.Compiler.IS_MINI && _base_token.getTokenType() == TokenType.boolPrimitive){
			allow_array = false; //mini only: non-int[] arrays not allowed
		}
		
		// System.out.println();
		// System.out.println("Parsing type: " + _base_token.getTokenType());
		if (allow_array){
			while (acceptOptional(TokenType.lsquare)){
				accept(TokenType.rsquare);
			}
		}
	}


	private int acceptListOf(TokenType ...t){
		accept(t);
		int i = 1;
		while (acceptOptional(TokenType.comma)){
			i++;
			accept(t);
		}
		return i;
	}




	private boolean acceptAnyOptional(Iterable<TokenType> types){
		//unlike acceptOptional, will only accept one of the types
		for (TokenType t : types){
			if (acceptOptional(t)){
				return true;
			}
		}
		return false;
	}
	private boolean acceptAnyOptional(TokenType ...types){
		return acceptAnyOptional(() -> Arrays.stream(types).iterator());
	}



	private boolean acceptOptional(TokenType expectedType) {
		if (_currentToken.getTokenType() == expectedType){
			_currentToken = _scanner.scan();
			return true;
		}
		return false;
	}
	private void acceptOptional(TokenType ...expectedType) {
		for (TokenType t : expectedType){
			acceptOptional(t);
		}
	}



	private void acceptAny(TokenType ...types) throws SyntaxError {
		acceptAny(() -> Arrays.stream(types).iterator());
	}

	private void acceptAny(Iterable<TokenType> tokens) throws SyntaxError {
		ArrayList<TokenType> ts = new ArrayList<>();
		for (TokenType t : tokens){
			ts.add(t);
			if (_currentToken.getTokenType() == t){
				_currentToken = _scanner.scan();
				return;
			}
		}
		String tokenString = null;
		for (TokenType t : tokens){
			if (tokenString == null){
				tokenString = "";
			} else {
				tokenString += ", ";
			}
			tokenString += t;
		}

		err(new SyntaxError("Unexpected Token: " + _currentToken + " does not match any of expected types " + 
						tokenString));
	}

	// This method will accept the token and retrieve the next token.
	//  Can be useful if you want to error check and accept all-in-one.
	private void accept(TokenType expectedType) throws SyntaxError {
		if( _currentToken.getTokenType() == expectedType ) {
			_currentToken = _scanner.scan();
			return;
		}
		err(new SyntaxError("Unexpected Token: " + _currentToken + " is not of type " + expectedType));
	}

	private void accept(TokenType ...expectedType) throws SyntaxError {
		for (TokenType t : expectedType){
			accept(t);
		}
	}

	private void err(SyntaxError err) throws SyntaxError {
		_errors.reportError(err);
		throw err;
	}
}
