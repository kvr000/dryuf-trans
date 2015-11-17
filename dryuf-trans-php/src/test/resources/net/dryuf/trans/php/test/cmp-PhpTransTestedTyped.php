<?php

namespace net\dryuf\trans\php\test;


/**
 * Comments to the class, more comments on next line including some multibyte unicode characters:
 * Žluťoučký kůň úpěl ďábelské ódy.
 * 
 * @author
 * 	Zbyněk Vyškovský
 * @since
 * 	2013-03-12
 */
class PhpTransTestedTyped extends \net\dryuf\core\Object
{
	/**
	 * Constructor comments.
	 */
	/**
	*/
	function			__construct()
	{
		$this->listField = new \net\dryuf\util\LinkedList();
		$this->mapField = new \net\dryuf\util\php\NativeHashMap();

		parent::__construct();
		$this->listField->add("hello");
		$this->mapField->put(0, "world");
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public function			constructorTest()
	{
		return strlen((new \net\dryuf\util\LinkedList())->get(0));
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public function			fieldTest()
	{
		$s = $this->listField->size();
		$l = strlen($this->listField->get(0));
		$v = strlen($this->mapField->get(0));
		return $s+$l+$v;
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public function			paramTest($listParam, $mapParam)
	{
		$s = $listParam->size();
		$l = strlen($listParam->get(0));
		$v = strlen($mapParam->get(0));
		return $s+$l+$v;
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public function			localTest()
	{
		$listVar = new \net\dryuf\util\LinkedList();
		$this->listField->add("hello");
		$mapVar = new \net\dryuf\util\php\NativeHashMap();
		$this->mapField->put(0, "world");
		$s = $listVar->size();
		$l = strlen($listVar->get(0));
		$v = strlen($mapVar->get(0));
		return $s+$l+$v;
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public function			methodMatchTest()
	{
		$i = 0;
		$i += \net\dryuf\core\Dryuf::createClassArg0('integer');
		$i += strlen(\net\dryuf\core\Dryuf::createClassArg0('string'));
		return $i;
	}

	/**
	@\net\dryuf\core\Type(type = 'net\dryuf\util\Listable<java\lang\String>')
	*/
	protected			$listField;

	/**
	@\net\dryuf\core\Type(type = 'net\dryuf\util\Map<java\lang\Long, java\lang\String>')
	*/
	protected			$mapField;
};


?>
