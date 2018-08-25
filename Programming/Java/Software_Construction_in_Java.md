# Software Construction in Java  
  
## Code Review  
Code review is careful, systematic study of source code by people who are not the original author of the code.  
Improving the code and the programmer  
Style Standards  
### 1. Don't Repeat Yourself (DRY)  
One reason why repeated code is bad is because a problem in the repeated code has to be fixed in many places, not just one.   
Avoid duplication like you'd avoid crossing the street without looking.   
### 2. Comments Where Needed  
the format of comment:   
```  
/**  
*objective  
* See http://.....  
*@param n what’s this n. Assumes what  
*@return what  
*/  
```  
obscure code should get a comment:  
```  
sendMessage("as you wish"); // this basically says "I love you"  
```  
### 3. Fail Fast  
set the type in advance  
check the range first  
end every multi-case by `throw new IllegalArgumentException("month out of range");`  
if statement (or switch statement) with an else clause   
### 4. Avoid Magic Numbers  
Avoid using numbers directly. You can use numbers with a comment, but a far better way is to declare the number as a named constant.  
```  
public static int dayOfYear(int month, int dayOfMonth, int year) {  
    if (month == 2) {   
        dayOfMonth += 31;  
    } else if (month == 3) {  
        dayOfMonth += 59;  
    } else if (month == 4) {  
        dayOfMonth += 90;  
    } else if (month == 5) {  
        dayOfMonth += 31 + 28 + 31 + 30;  
```  
* 2, …, 12 -> FEBRUARY, …, DECEMBER.  
* The days-of-months 30, 31, 28 -> MONTH_LENGTH[month].  
* The mysterious numbers 59 and 90 are particularly pernicious examples of magic numbers. Not only are they uncommented and undocumented, but they are also the result of a computation done by hand by the programmer. -> `MONTH_LENGTH[JANUARY] + MONTH_LENGTH[FEBRUARY]`  
  
Notice:  
`if (month == 2)` the magic number 2 might mean Feb or Mar  
good example:  
```  
final int fullCircleDegrees = 360;  
final int numSides = 5;  
final int sideLength = 36;  
for (int i = 0; i < numSides; ++i) {  
  turtle.forward(sideLength);  
  turtle.turn(fullCircleDegrees / numSides);  
}  
# safe from bugs (SFB, keep variabes' relations, like 'fullCircleDegrees / numSides' instead of using a constant number), easy to understand (ETU) and ready for change (RFC)  
  
```  
### 5. One Purpose For Each Variable  
Don't reuse parameters, and don't reuse variables. Introduce them freely, give them good names, and just stop using them when you stop needing them.  
It's a good idea to use final for method parameters, and as many other variables as you can.   
```  
public static int dayOfYear(final int month, final int dayOfMonth, final int year) { ... }  
```  
In Java, `tmp.charAt(2) == 5` can be executed. This may lead to bug(compares strings with numbers)  
### 6. Use Good Names  
Good method and variable names are long and self-descriptive.   
  
Follow the lexical naming conventions of the language.   
  
In Python, classes are typically Capitalized, variables are lowercase, and words_are_separated_by_underscores.   
  
In Java, methods(Method names are usually verb phrases, like `getDate` or `isUpperCase`) and variables AreNamedWithCamelCase, CONSTANTS_ARE_IN_ALL_CAPS, ClassesAreCapitalized, packages.are.lowercase.and.separated.by.dots.  
### 7. Use Whitespace To Help The Reader  
Never use tab characters for indentation, only space characters.Note that we say characters, not keys.  
### 8. Don't Use Global Variables(`public static`); Return, Don't Print  
In general, change global variables into parameters and return values, or put them inside objects that you're calling methods on.   
```  
public static void countLongWords(List<String> final words) {  
   int n = 0;  
   longestWord = "";  
   for (String final word: words) {  
       if (word.length() > LONG_WORD_LENGTH) ++n;  
       if (word.length() > longestWord.length()) longestWord = word;  
   }  
}  
```  
`final word`With final, the word variable now can't be reassigned within the body of the for loop. Each time around the for loop, however, it will get a fresh value.  
`final words`Adding final to the words variable means that the variable can't be reassigned -- but the List object that it points to can still be mutated.  
  
printing output shouldn't be a part of your design, only a part of how you debug your design.  
  
## Testing     
### Why Software Testing is Hard  
1. The purpose of validation is to uncover problems in a program and thereby increase your confidence in the program's correctness. Validation includes:  
  
* **Formal reasoning**: Verification is tedious to do by hand, and automated tool support for verification is still an active area of research. Nevertheless, small, crucial pieces of a program may be formally verified, such as the scheduler in an operating system, or the bytecode interpreter in a virtual machine, or the filesystem in an operating system.  
* **Code review**.   
* **Testing**. Running the program on carefully selected inputs and checking the results.   
  
2. Why Software Testing is Hard  
Exhaustive testing, Haphazard testing, Random or statistical testing(still doesn't work well for software)    
test cases must be chosen carefully and systematically  
  
### Partitioning  
  
#### Test-first Programming  
  
The development of a single function proceeds in this order:    
  
* Write a specification for the function.  
* Write tests that exercise the specification.  
* Write the actual code. Once your code passes the tests you wrote, you're done.  
  
#### Choosing Test Cases by Partitioning  
* how does the function works  
* boundaries:0, the maximum and minimum values of numeric types, like int and double, emptiness, the first and last element of a collection   
```  
BigInteger.multiply()  
/**  
 * @param val  another BigIntger  
 * @return a BigInteger whose value is (this * val).  
 */  
public BigInteger multiply(BigInteger val)  
```  
For example, here's how it might be used:  
`BigInteger a = ...;  
BigInteger b = ...;  
BigInteger ab = a.multiply(b);`  
even though only one parameter is explicitly shown in the method's declaration, multiply is actually a function of two arguments  
how multiplication works: one positive or both....  
special cases: 0, 1, -1  
a suspicious tester: we might suspect that the implementor of BigInteger might try to make it faster by using int or long internally. small, bigger than Long.MAX_VALUE, the biggest possible primitive integer in Java, which is roughly 2^63.  
  
The result partition 49(Full Cartesian product): 0, 1, -1  
small positive integer  
small negative integer  
huge positive integer  
huge negative integer  
  
```  
max()  
/**  
 * @param a  an argument  
 * @param b  another argument  
 * @return the larger of a and b.  
 */  
public static int max(int a, int b)  
```  
<, = ,>  
=0, >0, <0  
minimum integer  
maximum integer  
The result partition 5(Cover each part):  
a < b, a > 0, b > 0  
a > b, a < 0, b < 0  
a = b, a = 0, b = 0  
a < b, a = minint, b = maxint  
a > b, a = maxint, b = minint  
  
#### Two Extremes for Covering the Partition  
Often we strike some compromise between these two extremes, based on human judgement and caution, and influenced by whitebox testing and code coverage tools    

