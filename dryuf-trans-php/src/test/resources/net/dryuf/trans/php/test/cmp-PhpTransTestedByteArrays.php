<?php

namespace net\dryuf\trans\php\test;


/**
 * Class to test byte arrays.
 * 
 * @author
 * 	Zbyněk Vyškovský
 * @since
 * 	2015-10-28
 */
class PhpTransTestedByteArrays extends \net\dryuf\core\Object
{
	/**
	*/
	function			__construct()
	{
		parent::__construct();
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			createArrays()
	{
		$this->array = implode(array_map('chr', array_fill(0, 10, 0)));
		$this->array = implode(array_map('chr', array( 0, 1, 2 )));
	}

	/**
	@\net\dryuf\core\Type(type = 'byte')
	*/
	public function			readArray($i)
	{
		return ord($this->array[$i]);
	}

	/**
	@\net\dryuf\core\Type(type = 'byte[]')
	*/
	protected			$array;
};


?>
