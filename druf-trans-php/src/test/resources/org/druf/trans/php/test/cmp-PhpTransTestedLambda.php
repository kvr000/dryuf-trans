<?php

namespace org\druf\trans\php\test;


/**
 * Class to test parameterized types translation.
 * 
 * @author
 * 	Zbyněk Vyškovský
 * @since
 * 	2015-06-10
 */
class PhpTransTestedLambda extends \org\druf\core\Object
{
	/**
	*/
	function			__construct()
	{
		$this->stringList = new \org\druf\util\LinkedList();
		$this->stringSet = new \org\druf\util\php\StringNativeHashSet();

		parent::__construct();
	}

	/**
	@\org\druf\core\Type(type = 'int')
	*/
	public function			matchSimple()
	{
		$i = 0;
		$i += \org\druf\util\Collections::transform($this->stringList, function ($s) { return strlen($s); })->get(0);
		$i += strlen(\org\druf\util\Collections::transform($this->stringList, function ($s) { return $s."x"; })->get(0));
		$i += \org\druf\util\Sets::filter($this->stringSet, function ($s) { return strlen($s) > 0; })->size();
		$i += \org\druf\util\Sets::filter($this->stringSet, function ($s) { return strlen($s) > 0; })->size();
		return $i;
	}

	/**
	@\org\druf\core\Type(type = 'org\druf\util\Listable<java\lang\String>')
	*/
	protected			$stringList;

	/**
	@\org\druf\core\Type(type = 'org\druf\util\Set<java\lang\String>')
	*/
	protected			$stringSet;
};


?>
