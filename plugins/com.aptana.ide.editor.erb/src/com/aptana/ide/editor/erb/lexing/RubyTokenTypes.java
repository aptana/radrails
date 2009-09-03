/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain Eclipse Public Licensed code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ide.editor.erb.lexing;

import java.lang.reflect.Field;

import org.jruby.parser.Tokens;
import org.rubypeople.rdt.internal.ui.text.ruby.RubyTokenScanner;

/**
 * @author Kevin Sawicki (ksawicki@aptana.com)
 */
public class RubyTokenTypes
{

	public static final int ERROR = 0;
	// Keywords
	public static final int CLASS = 1;
	public static final int MODULE = 2;
	public static final int DEF = 3;
	public static final int UNDEF = 4;
	public static final int BEGIN = 5;
	public static final int RESCUE = 6;
	public static final int ENSURE = 7;
	public static final int END = 8;
	public static final int IF = 9;
	public static final int UNLESS = 10;
	public static final int THEN = 11;
	public static final int ELSEIF = 12;
	public static final int ELSE = 13;
	public static final int CASE = 14;
	public static final int WHEN = 15;
	public static final int WHILE = 16;
	public static final int UNTIL = 17;
	public static final int FOR = 18;
	public static final int BREAK = 19;
	public static final int NEXT = 20;
	public static final int REDO = 21;
	public static final int RETRY = 22;
	public static final int IN = 23;
	public static final int DO = 24;
	public static final int DO_COND = 25;
	public static final int DO_BLOCK = 26;
	public static final int RETURN = 27;
	public static final int YIELD = 28;
	public static final int SUPER = 29;
	public static final int SELF = 30;
	public static final int NIL = 31;
	public static final int TRUE = 32;
	public static final int FALSE = 33;
	public static final int AND = 34;
	public static final int OR = 35;
	public static final int NOT = 36;
	public static final int IF_MOD = 37;
	public static final int UNLESS_MOD = 38;
	public static final int WHILE_MOD = 39;
	public static final int UNTIL_MOD = 40;
	public static final int RESCUE_MOD = 41;
	public static final int ALIAS = 42;
	public static final int DEFINED = 43;
	public static final int L_BEGIN = 44;
	public static final int L_END = 45;
	public static final int __LINE__ = 46;
	public static final int __FILE__ = 47;

	// Identifiers
	public static final int IDENTIFIER = 48;
	public static final int FID = 49;
	public static final int GLOBAL_VARIABLE = 50;
	public static final int INSTANCE_VARIABLE = 51;
	public static final int CONSTANT = 52;
	public static final int CLASS_VARIABLE = 53;
	
	// Literals
	public static final int INTEGER = 54;
	public static final int FLOAT = 55;
	
	// Punctuators
	public static final int STRING_CONTENT = 56;
	public static final int STRING_BEG = 57;
	public static final int STRING_END = 58;
	public static final int STRING_DBEG = 59;
	public static final int STRING_DVAR = 60;
	public static final int XSTRING_BEG = 61;
	public static final int REGEXP_BEG = 62;
	public static final int REGEXP_END = 63;
	public static final int WORDS_BEG = 64;
	public static final int QWORDS_BEG = 65;
	public static final int BACK_REF = 66;
	public static final int BACK_REF2 = 67;
	public static final int NTH_REF = 68;
	public static final int UPLUS = 69;
	public static final int UMINUS = 70;
	public static final int UMINUS_NUM = 71;
	public static final int POW = 72;
	public static final int CMP = 73;
	public static final int EQ = 74;
	public static final int EQQ = 75;
	public static final int NEQ = 76;
	public static final int GEQ = 77;
	public static final int LEQ = 78;
	public static final int ANDOP = 79;
	public static final int OROP = 80;
	public static final int MATCH = 81;
	public static final int NMATCH = 82;
	public static final int DOT = 83;
	public static final int DOT2 = 84;
	public static final int DOT3 = 85;
	public static final int AREF = 86;
	public static final int LSHFT = 87;
	public static final int RSHFT = 88;
	public static final int COLON2 = 89;
	public static final int COLON3 = 90;
	public static final int OP_ASGN = 91;
	public static final int ASSOC = 92;
	public static final int LPAREN = 93;
	public static final int LPAREN2 = 94;
	public static final int RPAREN = 95;
	public static final int LPAREN_ARG = 96;
	public static final int LBRACK = 97;
	public static final int RBRACK = 98;
	public static final int LBRACE = 99;
	public static final int LBRACE_ARG = 100;
	public static final int STAR = 101;
	public static final int STAR2 = 102;
	public static final int AMPER = 103;
	public static final int AMPER2 = 104;
	// Identifiers
	public static final int SYMBOL = 105;
	// Punctuators
	public static final int TILDE = 106;
	public static final int PERCENT = 107;
	public static final int DIVIDE = 108;
	public static final int PLUS = 109;
	public static final int MINUS = 110;
	public static final int LT = 111;
	public static final int GT = 112;
	public static final int CARET = 113;
	public static final int BANG = 114;
	public static final int LCURLY = 115;
	public static final int RCURLY = 116;
	public static final int PIPE = 117;
	public static final int ASET = 118;
	public static final int COLON = 119;
	public static final int COMMA = 120;
	
	public static final int NEWLINE = 121;
	// Comments
	public static final int TASK_TAG = 122;
	public static final int SINGLE_LINE_COMMENT = 123;
	public static final int MULTI_LINE_COMMENT = 124;
		
	// Literals
	public static final int STRING = 125;
	public static final int REGEXP = 126;
	public static final int COMMAND = 127;
	public static final int CHARACTER = RubyTokenScanner.CHARACTER; // 128
	
	public static final int TOKEN_TYPES_LENGTH = 128;

	protected RubyTokenTypes()
	{

	}

	public static String getName(int type)
	{
		switch (type)
		{
			case ERROR:
				return "ERROR";
			case CLASS:
				return "CLASS";
			case MODULE:
				return "CLASS";
			case DEF:
				return "DEF";
			case UNDEF:
				return "UNDEF";
			case BEGIN:
				return "BEGIN";
			case RESCUE:
				return "RESCUE";
			case ENSURE:
				return "ENSURE";
			case END:
				return "END";
			case IF:
				return "IF";
			case UNLESS:
				return "UNLESS";
			case THEN:
				return "THEN";
			case ELSEIF:
				return "ELSEIF";
			case ELSE:
				return "ELSE";
			case CASE:
				return "CASE";
			case WHEN:
				return "WHEN";
			case WHILE:
				return "WHILE";
			case UNTIL:
				return "UNTIL";
			case FOR:
				return "FOR";
			case BREAK:
				return "BREAK";
			case NEXT:
				return "NEXT";
			case REDO:
				return "REDO";
			case RETRY:
				return "RETRY";
			case IN:
				return "IN";
			case DO:
				return "DO";
			case DO_COND:
				return "DO_COND";
			case DO_BLOCK:
				return "DO_BLOCK";
			case RETURN:
				return "RETURN";
			case YIELD:
				return "YIELD";
			case SUPER:
				return "SUPER";
			case SELF:
				return "SELF";
			case NIL:
				return "NIL";
			case TRUE:
				return "TRUE";
			case FALSE:
				return "FALSE";
			case AND:
				return "AND";
			case OR:
				return "OR";
			case NOT:
				return "NOT";
			case IF_MOD:
				return "IF_MOD";
			case UNLESS_MOD:
				return "UNLESS_MOD";
			case WHILE_MOD:
				return "WHILE_MOD";
			case UNTIL_MOD:
				return "UNTIL_MOD";
			case RESCUE_MOD:
				return "RESCUE_MOD";
			case ALIAS:
				return "ALIAS";
			case DEFINED:
				return "DEFINED";
			case L_BEGIN:
				return "L_BEGIN";
			case L_END:
				return "L_END";
			case __LINE__:
				return "__LINE__";
			case __FILE__:
				return "__FILE__";
			case IDENTIFIER:
				return "IDENTIFIER";
			case FID:
				return "FID";
			case GLOBAL_VARIABLE:
				return "GLOBAL_VARIABLE";
			case INSTANCE_VARIABLE:
				return "INTANCE_VARIABLE";
			case CONSTANT:
				return "CONSTANT";
			case CLASS_VARIABLE:
				return "CLASS_VARIABLE";
			case INTEGER:
				return "INTEGER";
			case FLOAT:
				return "FLOAT";
			case STRING_CONTENT:
				return "STRING_CONTENT";
			case STRING_BEG:
				return "STRING_BEG";
			case STRING_END:
				return "STRING_END";
			case STRING_DBEG:
				return "STRING_DBEG";
			case STRING_DVAR:
				return "STRING_DVAR";
			case XSTRING_BEG:
				return "XSTRING_BEG";
			case REGEXP_BEG:
				return "REGEXP_BEG";
			case REGEXP_END:
				return "REGEXP_END";
			case WORDS_BEG:
				return "WORDS_BEG";
			case QWORDS_BEG:
				return "QWORDS_BEG";
			case BACK_REF:
				return "BACK_REF";
			case BACK_REF2:
				return "BACK_REF2";
			case NTH_REF:
				return "NTH_REF";
			case UPLUS:
				return "UPLUS";
			case UMINUS:
				return "UMINUS";
			case UMINUS_NUM:
				return "UMINUS_NUM";
			case POW:
				return "POW";
			case CMP:
				return "CMP";
			case EQ:
				return "EQ";
			case EQQ:
				return "EQQ";
			case NEQ:
				return "NEQ";
			case GEQ:
				return "GEQ";
			case LEQ:
				return "LEQ";
			case ANDOP:
				return "ANDOP";
			case OROP:
				return "OROP";
			case MATCH:
				return "MATCH";
			case NMATCH:
				return "NMATCH";
			case DOT:
				return "DOT";
			case DOT2:
				return "DOT2";
			case DOT3:
				return "DOT3";
			case AREF:
				return "AREF";
			case LSHFT:
				return "LSHFT";
			case RSHFT:
				return "RSHFT";
			case COLON2:
				return "COLON2";
			case COLON3:
				return "COLON3";
			case OP_ASGN:
				return "OP_ASGN";
			case ASSOC:
				return "ASSOC";
			case LPAREN:
				return "LPAREN";
			case LPAREN2:
				return "LPAREN2";
			case RPAREN:
				return "RPAREN";
			case LPAREN_ARG:
				return "LPAREN_ARG";
			case LBRACK:
				return "LBRACK";
			case RBRACK:
				return "RBRACK";
			case LBRACE:
				return "LBRACE";
			case LBRACE_ARG:
				return "LBRACE_ARG";
			case STAR:
				return "STAR";
			case STAR2:
				return "STAR2";
			case AMPER:
				return "AMPER";
			case AMPER2:
				return "AMPER2";
			case SYMBOL:
				return "SYMBOL";
			case TILDE:
				return "TILDE";
			case PERCENT:
				return "PERCENT";
			case DIVIDE:
				return "DIVIDE";
			case PLUS:
				return "PLUS";
			case MINUS:
				return "MINUS";
			case LT:
				return "LT";
			case GT:
				return "GT";
			case CARET:
				return "CARET";
			case BANG:
				return "BANG";
			case LCURLY:
				return "LCURLY";
			case RCURLY:
				return "RCURLY";
			case PIPE:
				return "PIPE";
			case ASET:
				return "ASET";
			case COMMA:
				return "COMMA";
			case COLON:
				return "COLON";
			case NEWLINE:
				return "NEWLINE";
			case TASK_TAG:
				return "TASK_TAG";
			case SINGLE_LINE_COMMENT:
				return "SINGLE_LINE_COMMENT";
			case MULTI_LINE_COMMENT:
				return "MULTI_LINE_COMMENT";
			case STRING:
				return "STRING";
			case REGEXP:
				return "REGULAR_EXPRESSION";
			case COMMAND:
				return "COMMAND";
			case CHARACTER:
				return "CHARACTER";				
			default:
				return "<unknown>"; //$NON-NLS-1$
		}
	}

	public static int getOurTokenType(int jrubyTokenType)
	{
		switch (jrubyTokenType)
		{
			case Tokens.yyErrorCode:
				return ERROR;
			case Tokens.kCLASS:
				return CLASS;
			case Tokens.kMODULE:
				return MODULE;
			case Tokens.kDEF:
				return DEF;
			case Tokens.kUNDEF:
				return UNDEF;
			case Tokens.kBEGIN:
				return BEGIN;
			case Tokens.kRESCUE:
				return RESCUE;
			case Tokens.kENSURE:
				return ENSURE;
			case Tokens.kEND:
				return END;
			case Tokens.kIF:
				return IF;
			case Tokens.kUNLESS:
				return UNLESS;
			case Tokens.kTHEN:
				return THEN;
			case Tokens.kELSIF:
				return ELSEIF;
			case Tokens.kELSE:
				return ELSE;
			case Tokens.kCASE:
				return CASE;
			case Tokens.kWHEN:
				return WHEN;
			case Tokens.kWHILE:
				return WHILE;
			case Tokens.kUNTIL:
				return UNTIL;
			case Tokens.kFOR:
				return FOR;
			case Tokens.kBREAK:
				return BREAK;
			case Tokens.kNEXT:
				return NEXT;
			case Tokens.kREDO:
				return REDO;
			case Tokens.kRETRY:
				return RETRY;
			case Tokens.kIN:
				return IN;
			case Tokens.kDO:
				return DO;
			case Tokens.kDO_COND:
				return DO_COND;
			case Tokens.kDO_BLOCK:
				return DO_BLOCK;
			case Tokens.kRETURN:
				return RETURN;
			case Tokens.kYIELD:
				return YIELD;
			case Tokens.kSUPER:
				return SUPER;
			case Tokens.kSELF:
				return SELF;
			case Tokens.kNIL:
				return NIL;
			case Tokens.kTRUE:
				return TRUE;
			case Tokens.kFALSE:
				return FALSE;
			case Tokens.kAND:
				return AND;
			case Tokens.kOR:
				return OR;
			case Tokens.kNOT:
				return NOT;
			case Tokens.kIF_MOD:
				return IF_MOD;
			case Tokens.kUNLESS_MOD:
				return UNLESS_MOD;
			case Tokens.kWHILE_MOD:
				return WHILE_MOD;
			case Tokens.kUNTIL_MOD:
				return UNTIL_MOD;
			case Tokens.kRESCUE_MOD:
				return RESCUE_MOD;
			case Tokens.kALIAS:
				return ALIAS;
			case Tokens.kDEFINED:
				return DEFINED;
			case Tokens.klBEGIN:
				return L_BEGIN;
			case Tokens.klEND:
				return L_END;
			case Tokens.k__LINE__:
				return __LINE__;
			case Tokens.k__FILE__:
				return __FILE__;
			case Tokens.tIDENTIFIER:
				return IDENTIFIER;
			case Tokens.tFID: // methods ending in '!' or '?'
				return FID;
			case Tokens.tGVAR:
				return GLOBAL_VARIABLE;
			case Tokens.tIVAR:
				return INSTANCE_VARIABLE;
			case Tokens.tCONSTANT:
				return CONSTANT;
			case Tokens.tCVAR:
				return CLASS_VARIABLE;
			case Tokens.tINTEGER:
				return INTEGER;
			case Tokens.tFLOAT:
				return FLOAT;
			case Tokens.tSTRING_CONTENT:
				return STRING_CONTENT;
			case Tokens.tSTRING_BEG:
				return STRING_BEG;
			case Tokens.tSTRING_END:
				return STRING_END;
			case Tokens.tSTRING_DBEG:
				return STRING_DBEG;
			case Tokens.tSTRING_DVAR:
				return STRING_DVAR;
			case Tokens.tREGEXP_BEG:
				return REGEXP_BEG;
			case Tokens.tREGEXP_END:
				return REGEXP_END;
			case Tokens.tWORDS_BEG:
				return WORDS_BEG;
			case Tokens.tQWORDS_BEG:
				return QWORDS_BEG;
			case Tokens.tBACK_REF:
				return BACK_REF;
			case Tokens.tBACK_REF2:
				return BACK_REF2;
			case Tokens.tNTH_REF:
				return NTH_REF;
			case Tokens.tUPLUS:
				return UPLUS;
			case Tokens.tUMINUS:
				return UMINUS;
			case Tokens.tUMINUS_NUM:
				return UMINUS_NUM;
			case Tokens.tPOW:
				return POW;
			case Tokens.tCMP:
				return CMP;
			case Tokens.tEQ:
			case 61:
				return EQ;
			case Tokens.tEQQ:
				return EQQ;
			case Tokens.tNEQ:
				return NEQ;
			case Tokens.tGEQ:
				return GEQ;
			case Tokens.tLEQ:
				return LEQ;
			case Tokens.tANDOP:
				return ANDOP;
			case Tokens.tOROP:
				return OROP;
			case Tokens.tMATCH:
				return MATCH;
			case Tokens.tNMATCH:
				return NMATCH;
			case Tokens.tDOT:
				return DOT;
			case Tokens.tDOT2:
				return DOT2;
			case Tokens.tDOT3:
				return DOT3;
			case Tokens.tAREF:
				return AREF;
			case Tokens.tASET:
				return ASET;
			case Tokens.tLSHFT:
				return LSHFT;
			case Tokens.tRSHFT:
				return RSHFT;
			case Tokens.tCOLON2:
				return COLON2;
			case Tokens.tCOLON3:
				return COLON3;
			case Tokens.tOP_ASGN:
				return OP_ASGN;
			case Tokens.tASSOC:
				return ASSOC;
			case Tokens.tLPAREN:
				return LPAREN;
			case Tokens.tLPAREN2:
				return LPAREN2;
			case Tokens.tRPAREN:
				return RPAREN;
			case Tokens.tLPAREN_ARG:
				return LPAREN_ARG;
			case Tokens.tLBRACK:
				return LBRACK;
			case Tokens.tRBRACK:
				return RBRACK;
			case Tokens.tLBRACE:
				return LBRACE;
			case Tokens.tLBRACE_ARG:
				return LBRACE_ARG;
			case Tokens.tSTAR:
				return STAR;
			case Tokens.tSTAR2:
				return STAR2;
			case Tokens.tAMPER:
				return AMPER;
			case Tokens.tAMPER2:
				return AMPER2;
			case Tokens.tSYMBEG:
				return SYMBOL;
			case Tokens.tTILDE:
				return TILDE;
			case Tokens.tPERCENT:
				return PERCENT;
			case Tokens.tDIVIDE:
				return DIVIDE;
			case Tokens.tPLUS:
				return PLUS;
			case Tokens.tMINUS:
				return MINUS;
			case Tokens.tLT:
				return LT;
			case Tokens.tGT:
				return GT;
			case Tokens.tCARET:
				return CARET;
			case Tokens.tBANG:
				return BANG;
			case Tokens.tLCURLY:
				return LCURLY;
			case Tokens.tRCURLY:
				return RCURLY;
			case Tokens.tPIPE:
				return PIPE;
			case 10:
				return NEWLINE;
			case 44:
				return COMMA;
			case 58:
				return COLON;
			case TASK_TAG: 
				return TASK_TAG;
			case SINGLE_LINE_COMMENT:
				return SINGLE_LINE_COMMENT;
			case MULTI_LINE_COMMENT:
				return MULTI_LINE_COMMENT;				
			case STRING:
				return STRING;
			case REGEXP:
				return REGEXP;
			case COMMAND:
				return COMMAND;	
			case CHARACTER:
				return CHARACTER;	
			default:
				return -1;
		}
	}

	/**
	 * getIntValue
	 * 
	 * @param name
	 * @return int
	 */
	public static int getIntValue(String name)
	{
		Class c = RubyTokenTypes.class;
		int result = -1;

		try
		{
			Field f = c.getField(name);

			result = f.getInt(c);
		}
		// fail silently
		catch (SecurityException e)
		{
		}
		catch (NoSuchFieldException e)
		{
		}
		catch (IllegalArgumentException e)
		{
		}
		catch (IllegalAccessException e)
		{
		}

		return result;
	}

}
