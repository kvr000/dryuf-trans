<?php

namespace org\druf\trans\php\test;


/**
 * Comments to the class, more comments on next line including some multibyte unicode characters:
 * Žluťoučký kůň úpěl ďábelské ódy.
 * 
 * @author
 * 	rat
 * @since
 * 	2013-03-12
 */
class PhpTransTested extends \org\druf\core\Object implements \java\lang\Comparable
{
	/**
	 * Constructor comments.
	 */
	/**
	*/
	function			__construct()
	{
		$this->list = array_fill(0, 5, 0);
		$this->complexInstanceInitedOne = new \org\druf\util\LinkedList();
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
	@\org\druf\core\Type(type = 'void')
	*/
	public function			blackHole($obj)
	{
	}

	/**
	@\org\druf\core\Type(type = 'int')
	*/
	public function			compareTo($s)
	{
		return strcmp($this->name, $s->name);
	}

	/**
	 * Method comments.
	 */
	/**
	@\org\druf\core\Type(type = 'java\lang\String')
	*/
	public function			packageTest()
	{
		return \org\druf\trans\php\test\PhpTransTested::staticAccessTest().\org\druf\trans\php\test\PhpTransTested::$str;
	}

	/**
	@\org\druf\core\Type(type = 'void')
	*/
	public function			assignment()
	{
		$this->magic = 11;
	}

	/**
	@\org\druf\core\Type(type = 'int')
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
	@\org\druf\core\Type(type = 'int')
	*/
	public function			addMagic($arg)
	{
		$this->assignment();
		return \org\druf\trans\php\test\PhpTransTested::addNumbers($this->magic, $arg);
	}

	/**
	@\org\druf\core\Type(type = 'java\lang\String')
	*/
	public function			superTest()
	{
		return parent::toString();
	}

	/**
	@\org\druf\core\Type(type = 'java\lang\String')
	*/
	public function			thisTest()
	{
		return strval($this);
	}

	/**
	@\org\druf\core\Type(type = 'java\lang\Object')
	*/
	public function			newClassTest()
	{
		$this->typedField = new \org\druf\util\LinkedList();
		return (new \org\druf\trans\php\test\PhpTransTested())->addMagic(0);
	}

	/**
	@\org\druf\core\Type(type = 'java\lang\Object')
	*/
	public function			newArrayTest()
	{
		return implode(array_map('chr', array( 0, 1, 4, 8 )));
	}

	/**
	@\org\druf\core\Type(type = 'int')
	*/
	public function			arrayAccessTest($i)
	{
		return $this->list[$i-1];
	}

	/**
	@\org\druf\core\Type(type = 'void')
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
			\org\druf\trans\php\test\PhpTransTested::addNumbers(4, 5);
			break;
		}
	}

	/**
	@\org\druf\core\Type(type = 'void')
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
	@\org\druf\core\Type(type = 'void')
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
	@\org\druf\core\Type(type = 'void')
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
	@\org\druf\core\Type(type = 'void')
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
	@\org\druf\core\Type(type = 'void')
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
	@\org\druf\core\Type(type = 'boolean')
	*/
	public function			instanceOfTest(\org\druf\core\Object $obj)
	{
		return $obj instanceof \org\druf\trans\php\test\PhpTransTested;
	}

	/**
	@\org\druf\core\Type(type = 'char')
	*/
	public function			tryTest()
	{
		try {
			\org\druf\core\StringWrap::charAt(self::$str, 0);
		}
		catch (\org\druf\core\Exception $ex) {
			throw new \org\druf\core\RuntimeException($ex);
		}
		try {
			return \org\druf\core\StringWrap::charAt(self::$str, 0);
		}
		finally {
			$this->magic++;
		}
	}

	/**
	@\org\druf\core\Type(type = 'java\lang\String')
	*/
	public function			concatStringTest($x)
	{
		$x = self::$str.$x;
		return $x.self::$str;
	}

	/**
	@\org\druf\core\Type(type = 'java\lang\String')
	*/
	public function			selectTest($x)
	{
		return \org\druf\trans\php\test\PhpTransTested::$str.$this->concatStringTest($x).$this->magic;
	}

	/**
	@\org\druf\core\Type(type = 'java\lang\String')
	*/
	public static function		staticAccessTest()
	{
		$r = "";
		$r = $r.\org\druf\trans\php\test\PhpTransTested::$str.\org\druf\trans\php\test\PhpTransTested::addNumbers(0, 5);
		$r = $r.\org\druf\trans\php\test\PhpTransTested::$str.\org\druf\trans\php\test\PhpTransTested::addNumbers(1, 2);
		$r = $r.self::$str.\org\druf\trans\php\test\PhpTransTested::addNumbers(3, 4);
		return $r;
	}

	/**
	@\org\druf\core\Type(type = 'org\druf\trans\php\test\PhpTransDep')
	*/
	public function			createDep(\org\druf\trans\php\test\PhpTransDep $passed)
	{
		return is_null($passed) ? new \org\druf\trans\php\test\PhpTransDep() : $passed;
	}

	/**
	@\org\druf\core\Type(type = 'int')
	*/
	public function			parenthesisTest($x, $y)
	{
		return ($x+$y)*$this->magic;
	}

	/**
	@\org\druf\core\Type(type = 'int')
	*/
	public static function		constantTest()
	{
		return self::MYCONST+\org\druf\trans\php\test\PhpTransTested::MYCONST;
	}

	/**
	@\org\druf\core\Type(type = 'int')
	*/
	public function			collectionsTest()
	{
		$arrList = new \org\druf\util\ArrayList();
		$arrList->add(8);
		$arrList->get(0);
		$this->typedField->add("aa");
		return $this->typedField->size();
	}

	/**
	@\org\druf\core\Type(type = 'java\lang\Object')
	*/
	public function			getParametrized($value)
	{
		return $value;
	}

	/**
	@\org\druf\core\Type(type = 'java\lang\Number')
	*/
	public function			getParametrizedExtend($value)
	{
		return $value;
	}

	/**
	@\org\druf\core\Type(type = 'org\druf\util\Collection<java\lang\String>')
	*/
	public function			testGCommonFunction($in, $arg)
	{
		$add = $arg*$this->magic;
		return \org\druf\util\Collections::transform($in, 
			function ($v) use ($arg, $add) {
				$t = $arg+$this->magic;
				return strval($v+$t+$add);
			}
		);
	}

	/**
	@\org\druf\core\Type(type = 'void')
	*/
	public function			dynamicDefaults($mandatory, $b = true, $i = 1, $s = "hello")
	{
		$this->magic = $b ? $i : \org\druf\core\Druf::parseInt($s);
	}

	/**
	@\org\druf\core\Type(type = 'java\lang\Thread\State')
	*/
	public function			enumReturn()
	{
		return \java\lang\Thread\State::RUNNABLE;
	}

	/**
	@\org\druf\core\Type(type = 'java\lang\Object')
	*/
	public function			enumTranslate()
	{
		return \java\lang\Thread\State::TERMINATED;
	}

	/**
	@\org\druf\core\Type(type = 'int')
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
	@\org\druf\core\Type(type = 'void')
	*/
	public function			testExactMethodMatch()
	{
		stream_put_contents(STDOUT, \org\druf\trans\php\test\PhpTransTested::staticAccessTest());
	}

	/**
	@\org\druf\core\Type(type = 'void')
	*/
	public function			dataStructuresTest()
	{
		$this->blackHole(new \org\druf\util\HashMap());
		$this->blackHole(new \org\druf\util\LinkedList());
		$this->blackHole(new \org\druf\util\HashSet());
	}

	/**
	@\org\druf\core\Type(type = 'void')
	*/
	public function			nativeDataStructuresTest()
	{
		$this->blackHole(new \org\druf\util\php\NativeHashMap());
		$this->blackHole(new \org\druf\util\php\StringNativeHashMap());
	}

	/**
	@\org\druf\core\Type(type = 'java\lang\String')
	*/
	protected			$name = "name";

	/**
	@\org\druf\core\Type(type = 'int[]')
	*/
	protected			$list;

	/**
	@\org\druf\core\Type(type = 'int')
	*/
	protected			$magic = 6;

	/**
	@\org\druf\core\Type(type = 'java\lang\String')
	*/
	public static			$str = "abcd";

	/**
	@\org\druf\core\Type(type = 'boolean')
	*/
	protected			$uninit = false;

	/**
	@\org\druf\core\Type(type = 'org\druf\util\Listable<java\lang\String>')
	*/
	protected			$typedField;

	/**
	@\org\druf\core\Type(type = 'int')
	*/
	const				MYCONST = 11;

	/**
	 * Field comments.
	 */
	/**
	@\org\druf\core\Type(type = 'java\lang\Object')
	@\javax\validation\constraints\NotNull
	*/
	protected			$myObject;

	/**
	@\org\druf\core\Type(type = 'org\druf\util\Listable<java\lang\String>')
	*/
	public static			$complexStaticInitedOne;

	/**
	@\org\druf\core\Type(type = 'java\lang\String[]')
	*/
	public static			$complexStaticInitedTwo;

	/**
	@\org\druf\core\Type(type = 'org\druf\util\Listable<java\lang\String>')
	*/
	public				$complexInstanceInitedOne;

	/**
	@\org\druf\core\Type(type = 'int[]')
	*/
	public				$complexInstanceInitedTwo;

	public static function		_initManualStatic()
	{
		self::$complexStaticInitedOne = new \org\druf\util\LinkedList();
		self::$complexStaticInitedTwo = array( "a", null );
	}

};

\org\druf\trans\php\test\PhpTransTested::_initManualStatic();


?>
