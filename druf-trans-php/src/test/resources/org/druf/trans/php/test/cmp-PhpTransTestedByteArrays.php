<?php

namespace org\druf\trans\php\test;


/**
 * Class to test byte arrays.
 * 
 * @author
 * 	Zbyněk Vyškovský
 * @since
 * 	2015-10-28
 */
class PhpTransTestedByteArrays extends \org\druf\core\Object
{
	/**
	*/
	function			__construct()
	{
		parent::__construct();
	}

	/**
	@\org\druf\core\Type(type = 'void')
	*/
	public function			createArrays()
	{
		$this->array = implode(array_map('chr', array_fill(0, 10, 0)));
		$this->array = implode(array_map('chr', array( 0, 1, 2 )));
	}

	/**
	@\org\druf\core\Type(type = 'byte')
	*/
	public function			readArray($i)
	{
		return ord($this->array[$i]);
	}

	/**
	@\org\druf\core\Type(type = 'byte[]')
	*/
	protected			$array;
};


?>
