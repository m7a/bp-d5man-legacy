#include <stdlib.h>
#include <string.h>

#include "slist.h"

struct list* alloc_list()
{
	struct list* list = malloc(sizeof(struct list));
	list->size  = 0;
	list->start = NULL;
	list->end   = NULL;
	return list;
}

int list_empty(struct list* list)
{
	return list->start == NULL;
}

void free_list(struct list* list)
{
	free_list_by_func(list, NULL);
}

void free_list_by_func(struct list* list, void (*func)(void*)) {
	struct list_data* i;
	struct list_data* bak;
	if(list == NULL)
		return;
	i = list->start;
	while(i != NULL) {
		if(func != NULL)
			func(i->data);
		free(i->data);
		bak = i;
		i = i->next;
		free(bak);
	}
	free(list);
}

void add_to_list(struct list* list, void* data)
{
	struct list_data* entry = malloc(sizeof(struct list_data));
	entry->next = NULL;
	entry->data = data;
	if(list->end == NULL)
		list->start = entry;
	else
		list->end->next = entry;
	list->end = entry;
	list->size++;
}
