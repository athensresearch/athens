import React from 'react';
import { Button, Text, Box } from "@chakra-ui/react";
import { CSS } from "@dnd-kit/utilities";
import { useSortable } from "@dnd-kit/sortable";
import { SidebarItem } from './RightSidebar';

export const Item = (props) => {
  const [width, setWidth] = React.useState(undefined);
  const ref = React.useRef();

  // Lightweight way to 'set' these items in the width they render at
  // Important for when the item is dragged
  React.useEffect(() => {
    if (ref.current) {
      const el = ref.current as HTMLElement;
      setWidth(el.getBoundingClientRect().width + "px")
    }
  }, [ref]);

  // const [_order, name, isUnread, isCurrent] = id;

  const {
    attributes,
    listeners,
    isDragging,
    setNodeRef,
    transform,
    transition
  } = useSortable({ id: props.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition
  };

  return (
    <Box
      ref={setNodeRef}
      style={style}
      opacity={isDragging ? 0.5 : 1}
      {...attributes}
      {...listeners}
    >
      <SidebarItem {...props}></SidebarItem>
    </Box>
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
