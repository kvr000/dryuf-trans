<?php

namespace net\dryuf\trans\php\test;


/**
 * Comments to the class, more comments on next line including some multibyte unicode characters:
 * Žluťoučký kůň úpěl ďábelské ódy.
 * 
 * @author
 * 	rat
 * @since
 * 	2013-03-12
 */
class PhpTransTested extends \net\dryuf\core\Object implements \java\lang\Comparable
{
	/**
	 * Constructor comments.
	 */
	/**
	*/
	function			__construct()
	{
		$this->list = array_fill(0, 5, 0);
		$this->complexInstanceInitedOne = new \net\dryuf\util\LinkedList();
		$this->complexInstanceInitedTwo = array( 0, 1 );

		parent::__construct();
		$this->uninit = false;
	}

	/**
	 * Black hole servers as a way to avoid unused warnings.
	 * 
	 * @param obj
	 * 	object to be formally used
	 */
	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			blackHole($obj)
	{
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public function			compareTo($s)
	{
		return strcmp($this->name, $s->name);
	}

	/**
	 * Method comments.
	 */
	/**
	@\net\dryuf\core\Type(type = 'java\lang\String')
	*/
	public function			packageTest()
	{
		return \net\dryuf\trans\php\test\PhpTransTested::staticAccessTest().\net\dryuf\trans\php\test\PhpTransTested::$str;
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			assignment()
	{
		$this->magic = 11;
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public static function		addNumbers($x, $y)
	{
		$tmp = $x+$y;
		{
			$adding = $y;
			$tmp -= $adding;
			$tmp = $tmp == 0 ? $tmp*2 : $tmp;
		}
		return $tmp;
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public function			addMagic($arg)
	{
		$this->assignment();
		return \net\dryuf\trans\php\test\PhpTransTested::addNumbers($this->magic, $arg);
	}

	/**
	@\net\dryuf\core\Type(type = 'java\lang\String')
	*/
	public function			superTest()
	{
		return parent::toString();
	}

	/**
	@\net\dryuf\core\Type(type = 'java\lang\String')
	*/
	public function			thisTest()
	{
		return strval($this);
	}

	/**
	@\net\dryuf\core\Type(type = 'java\lang\Object')
	*/
	public function			newClassTest()
	{
		$this->typedField = new \net\dryuf\util\LinkedList();
		return (new \net\dryuf\trans\php\test\PhpTransTested())->addMagic(0);
	}

	/**
	@\net\dryuf\core\Type(type = 'java\lang\Object')
	*/
	public function			newArrayTest()
	{
		return implode(array_map('chr', array( 0, 1, 4, 8 )));
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public function			arrayAccessTest($i)
	{
		return $this->list[$i-1];
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			switchTest($x)
	{
		switch ($x) {
		case 0:
			$this->assignment();
			break;

		case 1:
			$this->addMagic(0);
			/* fall through */
		default:
			\net\dryuf\trans\php\test\PhpTransTested::addNumbers(4, 5);
			break;
		}
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			ifTest($x)
	{
		if ($x == 0)
			$x = 1;
		if ($x == 1) {
			$x++;
			$x += 2;
		}
		if ($x == 0)
			$x = 1;
		elseif ($x == 1)
			$x = 2;
		else
			$x = 3;
		if ($x == 0) {
			$x = 1;
		}
		elseif ($x == 3) {
			$x = 2;
		}
		else {
			$x = 0;
		}
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			forTest($x)
	{
		for (;;)
			break;
		for ($i = 0; $i < 5; $i++) ;
		for ($i = 0; $i < 4; $i++) {
			$i++;
			continue;
		}
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			whileTest($x)
	{
		while ($x != 0)
			$x /= 2;
		while ($x < 0) {
			$x++;
			break;
		}
		while ($x < 0) {
			$x++;
			continue;
		}
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			doWhileTest($x)
	{
		do
			$x++;
		while ($x < 0);
		do {
			$x++;
		} while ($x < 0);
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			foreachTest($x)
	{
		foreach (array( 4, 5, 6 ) as $v)
			$x *= $v;
		foreach (array( 4, 5, 6 ) as $v) {
			$x *= $v;
			break;
		}
	}

	/**
	@\net\dryuf\core\Type(type = 'boolean')
	*/
	public function			instanceOfTest(\net\dryuf\core\Object $obj)
	{
		return $obj instanceof \net\dryuf\trans\php\test\PhpTransTested;
	}

	/**
	@\net\dryuf\core\Type(type = 'char')
	*/
	public function			tryTest()
	{
		try {
			\net\dryuf\core\StringWrap::charAt(self::$str, 0);
		}
		catch (\net\dryuf\core\Exception $ex) {
			throw new \net\dryuf\core\RuntimeException($ex);
		}
		try {
			return \net\dryuf\core\StringWrap::charAt(self::$str, 0);
		}
		finally {
			$this->magic++;
		}
	}

	/**
	@\net\dryuf\core\Type(type = 'java\lang\String')
	*/
	public function			concatStringTest($x)
	{
		$x = self::$str.$x;
		return $x.self::$str;
	}

	/**
	@\net\dryuf\core\Type(type = 'java\lang\String')
	*/
	public function			selectTest($x)
	{
		return \net\dryuf\trans\php\test\PhpTransTested::$str.$this->concatStringTest($x).$this->magic;
	}

	/**
	@\net\dryuf\core\Type(type = 'java\lang\String')
	*/
	public static function		staticAccessTest()
	{
		$r = "";
		$r = $r.\net\dryuf\trans\php\test\PhpTransTested::$str.\net\dryuf\trans\php\test\PhpTransTested::addNumbers(0, 5);
		$r = $r.\net\dryuf\trans\php\test\PhpTransTested::$str.\net\dryuf\trans\php\test\PhpTransTested::addNumbers(1, 2);
		$r = $r.self::$str.\net\dryuf\trans\php\test\PhpTransTested::addNumbers(3, 4);
		return $r;
	}

	/**
	@\net\dryuf\core\Type(type = 'net\dryuf\trans\php\test\PhpTransDep')
	*/
	public function			createDep(\net\dryuf\trans\php\test\PhpTransDep $passed)
	{
		return is_null($passed) ? new \net\dryuf\trans\php\test\PhpTransDep() : $passed;
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public function			parenthesisTest($x, $y)
	{
		return ($x+$y)*$this->magic;
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public static function		constantTest()
	{
		return self::MYCONST+\net\dryuf\trans\php\test\PhpTransTested::MYCONST;
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public function			collectionsTest()
	{
		$arrList = new \net\dryuf\util\ArrayList();
		$arrList->add(8);
		$arrList->get(0);
		$this->typedField->add("aa");
		return $this->typedField->size();
	}

	/**
	@\net\dryuf\core\Type(type = 'java\lang\Object')
	*/
	public function			getParametrized($value)
	{
		return $value;
	}

	/**
	@\net\dryuf\core\Type(type = 'java\lang\Number')
	*/
	public function			getParametrizedExtend($value)
	{
		return $value;
	}

	/**
	@\net\dryuf\core\Type(type = 'net\dryuf\util\Collection<java\lang\String>')
	*/
	public function			testGCommonFunction($in, $arg)
	{
		$add = $arg*$this->magic;
		return \net\dryuf\util\Collections::transform($in, 
			function ($v) use ($arg, $add) {
				$t = $arg+$this->magic;
				return strval($v+$t+$add);
			}
		);
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			dynamicDefaults($mandatory, $b = true, $i = 1, $s = "hello")
	{
		$this->magic = $b ? $i : \net\dryuf\core\Dryuf::parseInt($s);
	}

	/**
	@\net\dryuf\core\Type(type = 'java\lang\Thread\State')
	*/
	public function			enumReturn()
	{
		return \java\lang\Thread\State::RUNNABLE;
	}

	/**
	@\net\dryuf\core\Type(type = 'java\lang\Object')
	*/
	public function			enumTranslate()
	{
		return \java\lang\Thread\State::TERMINATED;
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public function			enumSwitch($method)
	{
		switch ($method) {
		case \java\lang\Thread\State::BLOCKED:
			return 0;

		case \java\lang\Thread\State::RUNNABLE:
			return 4;

		case \java\lang\Thread\State::WAITING:
			return 5;

		default:
			return -1;
		}
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			testExactMethodMatch()
	{
		stream_put_contents(STDOUT, \net\dryuf\trans\php\test\PhpTransTested::staticAccessTest());
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			dataStructuresTest()
	{
		$this->blackHole(new \net\dryuf\util\HashMap());
		$this->blackHole(new \net\dryuf\util\LinkedList());
		$this->blackHole(new \net\dryuf\util\HashSet());
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			nativeDataStructuresTest()
	{
		$this->blackHole(new \net\dryuf\util\php\NativeHashMap());
		$this->blackHole(new \net\dryuf\util\php\StringNativeHashMap());
	}

	/**
	@\net\dryuf\core\Type(type = 'java\lang\String')
	*/
	protected			$name = "name";

	/**
	@\net\dryuf\core\Type(type = 'int[]')
	*/
	protected			$list;

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	protected			$magic = 6;

	/**
	@\net\dryuf\core\Type(type = 'java\lang\String')
	*/
	public static			$str = "abcd";

	/**
	@\net\dryuf\core\Type(type = 'boolean')
	*/
	protected			$uninit = false;

	/**
	@\net\dryuf\core\Type(type = 'net\dryuf\util\Listable<java\lang\String>')
	*/
	protected			$typedField;

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	const				MYCONST = 11;

	/**
	 * Field comments.
	 */
	/**
	@\net\dryuf\core\Type(type = 'java\lang\Object')
	@\javax\validation\constraints\NotNull
	*/
	protected			$myObject;

	/**
	@\net\dryuf\core\Type(type = 'net\dryuf\util\Listable<java\lang\String>')
	*/
	public static			$complexStaticInitedOne;

	/**
	@\net\dryuf\core\Type(type = 'java\lang\String[]')
	*/
	public static			$complexStaticInitedTwo;

	/**
	@\net\dryuf\core\Type(type = 'net\dryuf\util\Listable<java\lang\String>')
	*/
	public				$complexInstanceInitedOne;

	/**
	@\net\dryuf\core\Type(type = 'int[]')
	*/
	public				$complexInstanceInitedTwo;

	public static function		_initManualStatic()
	{
		self::$complexStaticInitedOne = new \net\dryuf\util\LinkedList();
		self::$complexStaticInitedTwo = array( "a", null );
	}

};

\net\dryuf\trans\php\test\PhpTransTested::_initManualStatic();


?>
