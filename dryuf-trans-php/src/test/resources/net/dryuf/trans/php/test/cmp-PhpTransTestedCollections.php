<?php

namespace net\dryuf\trans\php\test;


/**
 * Comments to the class, more comments on next line including some multibyte unicode characters:
 * Žluťoučký kůň úpěl ďábelské ódy.
 * 
 * @author
 * 	rat
 * @since
 * 	2015-06-11
 */
class PhpTransTestedCollections extends \net\dryuf\core\Object
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
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			blackHole($obj)
	{
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
	@\net\dryuf\core\Type(type = 'int')
	*/
	public function			collectionMethods()
	{
		(new \net\dryuf\util\php\StringNativeHashSet())->forEachDo(function ($value) { return strlen($value); });
		return 0;
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public function			mapMethods()
	{
		(new \net\dryuf\util\php\StringNativeHashMap())->forEachDo(function ($key, $key) { return strlen($value); });
		return 0;
	}
};


?>
