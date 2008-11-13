/* Generated By:JavaCC: Do not edit this line. OQLParserConstants.java */
package org.eclipse.mat.parser.internal.oql.parser;

/**
 * Token literal values and constants. Generated by
 * org.javacc.parser.OtherFilesGen#start()
 */
public interface OQLParserConstants
{

    /** End of File. */
    int EOF = 0;
    /** RegularExpression Id. */
    int SINGLE_LINE_COMMENT = 9;
    /** RegularExpression Id. */
    int FORMAL_COMMENT = 10;
    /** RegularExpression Id. */
    int MULTI_LINE_COMMENT = 11;
    /** RegularExpression Id. */
    int INTEGER_LITERAL = 13;
    /** RegularExpression Id. */
    int LONG_LITERAL = 14;
    /** RegularExpression Id. */
    int DECIMAL_LITERAL = 15;
    /** RegularExpression Id. */
    int HEX_LITERAL = 16;
    /** RegularExpression Id. */
    int OCTAL_LITERAL = 17;
    /** RegularExpression Id. */
    int FLOATING_POINT_LITERAL = 18;
    /** RegularExpression Id. */
    int EXPONENT = 19;
    /** RegularExpression Id. */
    int CHARACTER_LITERAL = 20;
    /** RegularExpression Id. */
    int STRING_LITERAL = 21;
    /** RegularExpression Id. */
    int INSTANCEOF = 22;
    /** RegularExpression Id. */
    int TRUE = 23;
    /** RegularExpression Id. */
    int FALSE = 24;
    /** RegularExpression Id. */
    int OR = 25;
    /** RegularExpression Id. */
    int AND = 26;
    /** RegularExpression Id. */
    int NULL = 27;
    /** RegularExpression Id. */
    int IMPLEMENTS = 28;
    /** RegularExpression Id. */
    int DOLLAR_SIGN = 29;
    /** RegularExpression Id. */
    int IDENTIFIER = 30;
    /** RegularExpression Id. */
    int LETTER = 31;
    /** RegularExpression Id. */
    int PART_LETTER = 32;
    /** RegularExpression Id. */
    int LT = 33;
    /** RegularExpression Id. */
    int BANG = 34;
    /** RegularExpression Id. */
    int EQ = 35;
    /** RegularExpression Id. */
    int LE = 36;
    /** RegularExpression Id. */
    int GE = 37;
    /** RegularExpression Id. */
    int NE = 38;

    /** Lexical state. */
    int DEFAULT = 0;
    /** Lexical state. */
    int IN_SINGLE_LINE_COMMENT = 1;
    /** Lexical state. */
    int IN_FORMAL_COMMENT = 2;
    /** Lexical state. */
    int IN_MULTI_LINE_COMMENT = 3;

    /** Literal token values. */
    String[] tokenImage = { "<EOF>", "\" \"", "\"\\t\"", "\"\\n\"", "\"\\r\"", "\"\\f\"", "\"//\"",
                    "<token of kind 7>", "\"/*\"", "<SINGLE_LINE_COMMENT>", "\"*/\"", "\"*/\"", "<token of kind 12>",
                    "<INTEGER_LITERAL>", "<LONG_LITERAL>", "<DECIMAL_LITERAL>", "<HEX_LITERAL>", "<OCTAL_LITERAL>",
                    "<FLOATING_POINT_LITERAL>", "<EXPONENT>", "<CHARACTER_LITERAL>", "<STRING_LITERAL>",
                    "\"INSTANCEOF\"", "\"true\"", "\"false\"", "\"or\"", "\"and\"", "\"null\"", "\"implements\"",
                    "\"$\"", "<IDENTIFIER>", "<LETTER>", "<PART_LETTER>", "\"<\"", "\"!\"", "\"==\"", "\"<=\"",
                    "\">=\"", "\"!=\"", "\";\"", "\"*\"", "\",\"", "\".\"", "\"{\"", "\"}\"", "\"@\"", "\"(\"",
                    "\")\"", "\"[]\"", "\"=\"", "\">\"", "\"+\"", "\"-\"", "\"/\"", };

}
