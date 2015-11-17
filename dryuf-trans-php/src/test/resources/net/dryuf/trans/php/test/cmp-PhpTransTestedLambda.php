<?php

namespace net\dryuf\trans\php\test;


/**
 * Class to test parameterized types translation.
 * 
 * @author
 * 	Zbyněk Vyškovský
 * @since
 * 	2015-06-10
 */
class PhpTransTestedLambda extends \net\dryuf\core\Object
{
	/**
	*/
	function			__construct()
	{
		$this->stringList = new \net\dryuf\util\LinkedList();
		$this->stringSet = new \net\dryuf\util\php\StringNativeHashSet();

		parent::__construct();
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public function			matchSimple()
	{
		$i = 0;
		$i += \net\dryuf\util\Collections::transform($this->stringList, function ($s) { return strlen($s); })->get(0);
		$i += strlen(\net\dryuf\util\Collections::transform($this->stringList, function ($s) { return $s."x"; })->get(0));
		$i += \net\dryuf\util\Sets::filter($this->stringSet, function ($s) { return strlen($s) > 0; })->size();
		$i += \net\dryuf\util\Sets::filter($this->stringSet, function ($s) { return strlen($s) > 0; })->size();
		return $i;
	}

	/**
	@\net\dryuf\core\Type(type = 'net\dryuf\util\Listable<java\lang\String>')
	*/
	protected			$stringList;

	/**
	@\net\dryuf\core\Type(type = 'net\dryuf\util\Set<java\lang\String>')
	*/
	protected			$stringSet;
};


?>
