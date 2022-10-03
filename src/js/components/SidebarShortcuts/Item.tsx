import React from 'react';
import { Button, Text, Box } from "@chakra-ui/react";
import { CSS } from "@dnd-kit/utilities";
import { useSortable } from "@dnd-kit/sortable";

export const Item = (props) => {
  const { id, children, ...rest } = props;
  const [_order, name, isUnread] = id;

  const {
    attributes,
    listeners,
    isDragging,
    setNodeRef,
    transform,
    transition
  } = useSortable({ id });

  return (
    <Button
      size="sm"
      flexShrink={0}
      px={3}
      variant="ghost"
      justifyContent="flex-start"
      overflow="hidden"
      ref={setNodeRef}
      transform={CSS.Transform.toString(transform)}
      transition={transition}
      opacity={isDragging ? 0.5 : 1}
      {...attributes}
      {...listeners}
      {...rest}
    >
      <Text
        as="span"
        textAlign="start"
        flex="1 1 100%"
        fontWeight={isUnread ? "bold" : "n"}
        overflow="hidden"
        textOverflow="ellipsis">
        {name}
      </Text>
      {children}
    </Button>
  );
};

export const ItemDragOverlay = (props) => {
  return (
    <Item {...props}><Box
      bg="background.floor"
      position="absolute"
      inset="0"
      borderRadius="inherit"
      boxShadow="page"
      zIndex={-1}
    /></Item>
  );
};
