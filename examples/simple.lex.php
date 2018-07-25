<?php

class Yylex extends JLexPHP\Base  {
	const YY_BUFFER_SIZE = 512;
	const YY_F = -1;
	const YY_NO_STATE = -1;
	const YY_NOT_ACCEPT = 0;
	const YY_START = 1;
	const YY_END = 2;
	const YY_NO_ANCHOR = 4;
	const YY_BOL = 128;
	const YY_EOF = 129;

//<YYINITIAL> L? \" (\\.|[^\\\"])* \"	{ $this->createToken(CParser::TK_STRING_LITERAL); }
	/* blah */
	protected $yy_count_chars = true;
	protected $yy_count_lines = true;

	public function __construct($stream) {
		parent::__construct($stream);
		$this->yy_lexical_state = self::YYINITIAL;
	}

	const YYINITIAL = 0;
	const COMMENTS = 1;
	static $yy_state_dtrans = [
		0,
		13
	];
	static $yy_acpt = [
		/* 0 */ self::YY_NOT_ACCEPT,
		/* 1 */ self::YY_NO_ANCHOR,
		/* 2 */ self::YY_NO_ANCHOR,
		/* 3 */ self::YY_NO_ANCHOR,
		/* 4 */ self::YY_NO_ANCHOR,
		/* 5 */ self::YY_NO_ANCHOR,
		/* 6 */ self::YY_NO_ANCHOR,
		/* 7 */ self::YY_NO_ANCHOR,
		/* 8 */ self::YY_NO_ANCHOR,
		/* 9 */ self::YY_NO_ANCHOR,
		/* 10 */ self::YY_NO_ANCHOR,
		/* 11 */ self::YY_NO_ANCHOR,
		/* 12 */ self::YY_NO_ANCHOR,
		/* 13 */ self::YY_NOT_ACCEPT
	];
		static $yy_cmap = [
 8, 8, 8, 8, 8, 8, 8, 8, 8, 9, 2, 8, 9, 9, 8, 8, 8, 8, 8, 8,
 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9, 8, 8, 8, 8, 8, 8, 8,
 8, 8, 5, 3, 8, 4, 8, 6, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 7,
 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
 8, 8, 8, 8, 8, 8, 8, 8, 0, 0,];

		static $yy_rmap = [
 0, 1, 2, 3, 1, 1, 1, 4, 1, 1, 1, 1, 1, 5,];

		static $yy_nxt = [
[
 1, 2, 3, 4, 5, 6, 7, 8, 9, 3,
],
[
 -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
],
[
 -1, 2, -1, -1, -1, -1, -1, -1, -1, -1,
],
[
 -1, -1, 3, -1, -1, -1, -1, -1, -1, 3,
],
[
 -1, -1, -1, -1, -1, -1, 10, -1, -1, -1,
],
[
 1, 11, 12, 11, 11, 11, 11, 11, 11, 11,
],
];

	public function /*Yytoken*/ nextToken () {
		$yy_anchor = self::YY_NO_ANCHOR;
		$yy_state = self::$yy_state_dtrans[$this->yy_lexical_state];
		$yy_next_state = self::YY_NO_STATE;
		$yy_last_accept_state = self::YY_NO_STATE;
		$yy_initial = true;

		$this->yy_mark_start();

		$yy_this_accept = self::$yy_acpt[$yy_state];
		if (self::YY_NOT_ACCEPT !== $yy_this_accept) {
			$yy_last_accept_state = $yy_state;
			$this->yy_mark_end();
		}

		while (true) {
			$yy_lookahead = self::YY_BOL;

			if (!$yy_initial || !$this->yy_at_bol) {
				$yy_lookahead = $this->yy_advance();
			}

			$yy_next_state = self::$yy_nxt[self::$yy_rmap[$yy_state]][self::$yy_cmap[$yy_lookahead]];

			if (self::YY_EOF === $yy_lookahead && $yy_initial) {
				return null;
			}

			if (self::YY_F !== $yy_next_state) {
				$yy_state = $yy_next_state;
				$yy_initial = false;

				$yy_this_accept = self::$yy_acpt[$yy_state];
				if (self::YY_NOT_ACCEPT !== $yy_this_accept) {
					$yy_last_accept_state = $yy_state;
					$this->yy_mark_end();
				}
			} else {
				if (self::YY_NO_STATE === $yy_last_accept_state) {
					throw new \Exception("Lexical Error: Unmatched Input.");
				} else {
					$yy_anchor = self::$yy_acpt[$yy_last_accept_state];

					if (0 !== (self::YY_END & $yy_anchor)) {
						$this->yy_move_end();
					}

					$this->yy_to_mark();

					switch ($yy_last_accept_state) {
					case 1: 
					case -2:
						break;

					case 2: { return $this->createToken(); }
					case -3:
						break;

					case 3: {}
					case -4:
						break;

					case 4: { return $this->createToken(); }
					case -5:
						break;

					case 5: { return $this->createToken(); }
					case -6:
						break;

					case 6: { return $this->createToken(); }
					case -7:
						break;

					case 7: { return $this->createToken(); }
					case -8:
						break;

					case 8: { return $this->createToken(); }
					case -9:
						break;

					case 9: { throw new Exception("bah!"); }
					case -10:
						break;

					case 10: { $this->yybegin(self::COMMENTS); }
					case -11:
						break;

					case 11: {}
					case -12:
						break;

					case 12: { $this->yybegin(self::YYINITIAL); }
					case -13:
						break;

						default:
							$this->yy_error('INTERNAL', false);

						case -1:

					}

					$yy_initial = true;
					$yy_state = self::$yy_state_dtrans[$this->yy_lexical_state];
					$yy_next_state = self::YY_NO_STATE;
					$yy_last_accept_state = self::YY_NO_STATE;

					$this->yy_mark_start();

					$yy_this_accept = self::$yy_acpt[$yy_state];
					if (self::YY_NOT_ACCEPT !== $yy_this_accept) {
						$yy_last_accept_state = $yy_state;
						$this->yy_mark_end();
					}
				}
			}
		}
	}
}
