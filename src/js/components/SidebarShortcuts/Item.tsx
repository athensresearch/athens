import React from 'react';
import { Button, Text, Box } from "@chakra-ui/react";
import { CSS } from "@dnd-kit/utilities";
import { useSortable } from "@dnd-kit/sortable";

export const Item = (props) => {
  const { id, children, ...rest } = props;
  const ref = React.useRef();

  const [_order, name, isUnread, isCurrent] = id;

  const {
    attributes,
    listeners,
    isDragging,
    setNodeRef,
    transform,
    transition
  } = useSortable({ id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition
  };

  return (
    <Button
      size="sm"
      flexShrink={0}
      variant="ghost"
      isActive={isCurrent}
      justifyContent="flex-start"
      overflow="hidden"
      ref={setNodeRef}
      style={style}
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
