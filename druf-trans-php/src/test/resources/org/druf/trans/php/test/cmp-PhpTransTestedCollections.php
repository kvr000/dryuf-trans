<?php

namespace org\druf\trans\php\test;


/**
 * Comments to the class, more comments on next line including some multibyte unicode characters:
 * Žluťoučký kůň úpěl ďábelské ódy.
 * 
 * @author
 * 	rat
 * @since
 * 	2015-06-11
 */
class PhpTransTestedCollections extends \org\druf\core\Object
{
	/**
	 * Constructor comments.
	 */
	/**
	*/
	function			__construct()
	{
		parent::__construct();
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
	@\org\druf\core\Type(type = 'int')
	*/
	public function			collectionMethods()
	{
		(new \org\druf\util\php\StringNativeHashSet())->forEachDo(function ($value) { return strlen($value); });
		return 0;
	}

	/**
	@\org\druf\core\Type(type = 'int')
	*/
	public function			mapMethods()
	{
		(new \org\druf\util\php\StringNativeHashMap())->forEachDo(function ($key, $key) { return strlen($value); });
		return 0;
	}
};


?>
