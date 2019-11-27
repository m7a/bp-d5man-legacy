struct parse_callback {
	/*
	 * Warning: The first fields till "end meta fields" are ordered exactly
	 *          like in meta.h. This is not coincidence but a requirement.
	 *          @OOP: We implement poor man's "extends" for C with that!
	 */
	void* data;
	void (*begin_document)(void* data);
	void (*meta_kv)(void* data, char* key, char* value);
	void (*meta_tex)(void* data, char* tex);
	void (*meta_end)(void* data);

	void (*end_document)(void* data);
	void (*begin_tex)(void* data);
	void (*begin_tex_math)(void* data);
	void (*begin_tex_html)(void* data);
	void (*texchar)(void* data, char chr);
	void (*end_tex)(void* data);
	void (*end_tex_math)(void* data);
	void (*end_tex_html)(void* data);
	void (*section)(void* data, char* title);
	void (*subsection)(void* data, char* title);
	void (*cdata)(void* data, char* cdata);
	void (*paragraph)(void* data);
	void (*half_space)(void* data);
	void (*forced_space)(void* data);
	void (*link)(void* data, char* dest, char* title);
	void (*begin_emphasis)(void* data);
	void (*end_emphasis)(void* data);
	void (*begin_english_quot)(void* data);
	void (*end_english_quot)(void* data);
	void (*begin_german_quot)(void* data);
	void (*end_german_quot)(void* data);
	void (*sym_rightarrow1)(void* data);
	void (*sym_rightarrow2)(void* data);
	void (*sym_leftarrow)(void* data);
	void (*sym_dots)(void* data);
	void (*sym_smiley)(void* data);
	void (*sym_math_in)(void* data);
	void (*sym_dash)(void* data);
	void (*sym_exclamation)(void* data);
	void (*exp)(void* data, char* a, char* b);
	void (*begin_shortcut)(void* data);
	void (*shortcut_key)(void* data, char* key);
	void (*end_shortcut)(void* data);
	void (*begin_code_inline)(void* data);
	void (*end_code_inline)(void* data);
	void (*begin_table)(void* data, char* caption);
	void (*begin_table_field)(void* data);
	void (*end_table_field)(void* data);
	void (*table_newline)(void* data);
	void (*table_mid_sep)(void* data);
	void (*end_table)(void* data);
	void (*begin_list_group)(void* data);
	void (*begin_list)(void* data, char lc);
	void (*begin_list_description_title)(void* data);
	void (*end_list_description_title)(void* data);
	void (*begin_list_item)(void* data);
	void (*end_list_item)(void* data);
	void (*end_list)(void* data);
	void (*end_list_group)(void* data);
	void (*begin_code)(void* data);
	void (*end_code)(void* data);
};
