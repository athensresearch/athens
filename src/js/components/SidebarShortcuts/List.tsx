import * as React from "react";
import { VStack, Box, Portal, AvatarGroup, Avatar } from "@chakra-ui/react";
import {
  DndContext,
  useSensors,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  DragOverlay,
  useSensor
} from "@dnd-kit/core";
import {
  SortableContext,
  verticalListSortingStrategy,
  sortableKeyboardCoordinates,
  arrayMove
} from "@dnd-kit/sortable";
import { Item, ItemDragOverlay } from "./Item";

// configuration
const activationConstraint = {
  distance: 15,
}


export const List = (props) => {
  const {
    items: outerItems,
    children,
    onUpdateItemsOrder,
    onOpenItem,
    ...rest
  } = props;
  const container = React.useRef();
  const [containerWidth, setContainerWidth] = React.useState("unset");

  // Maintain an internal list of items for proper animation
  const [items, setItems] = React.useState(outerItems);
  const [activeId, setActiveId] = React.useState(null);

  React.useEffect(() => {
    setItems(outerItems)
  }, [outerItems])

  React.useEffect(() => {
    if (container.current) {
      const el = container.current as HTMLElement;
      setContainerWidth(el.getBoundingClientRect().width.toString());
    }
  }, [container]);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates
    })
  );

  const handleDragStart = (e) => {
    setActiveId(e.active.id);
  };

  const handleDragEnd = (e) => {
    const { active, over } = e;
    setActiveId(null);

    if (active.id !== over.id) {
      setItems((items) => {
        const oldIndex = items.indexOf(active.id);
        const newIndex = items.indexOf(over.id);
        onUpdateItemsOrder(oldIndex, newIndex);
        return arrayMove(items, oldIndex, newIndex);
      });
    }
  };

  return (
    <Box sx={{ "--parentWidth": containerWidth }}>
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragEnd={handleDragEnd}
        onDragStart={handleDragStart}
      >
        <VStack spacing={0.25} align="stretch" overflowX="hidden" {...rest}>
          <SortableContext strategy={verticalListSortingStrategy} items={items}>
            {items.map(item => <Item
              onClick={(e) => onOpenItem(e, item)}
              key={item}
              id={item}></Item>)}
          </SortableContext>
        </VStack>

        <Portal>
          <DragOverlay>
            {activeId ? <ItemDragOverlay sx={{ "--parentWidth": containerWidth }} key={activeId} id={activeId} /> : null}
          </DragOverlay>
        </Portal>
      </DndContext>
    </Box>
  );
};
