import { Button, Box } from "@chakra-ui/react";
import { CSS } from "@dnd-kit/utilities";
import { useSortable } from "@dnd-kit/sortable";

export const Item = (props) => {
  const { id, children, ...rest } = props;

  const [_order, name] = id;

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
      as="li"
      size="sm"
      variant="ghost"
      width="14em"
      justifyContent="flex-start"
      ref={setNodeRef}
      style={style}
      opacity={isDragging ? 0.5 : 1}
      {...attributes}
      {...listeners}
      {...rest}
    >
      {name}
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
