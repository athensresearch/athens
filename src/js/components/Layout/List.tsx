import * as React from "react";
import { Portal } from "@chakra-ui/react";
import {
  DndContext,
  useSensors,
  closestCenter,
  PointerSensor,
  DragOverlay,
  useSensor
} from "@dnd-kit/core";
import {
  SortableContext,
  verticalListSortingStrategy,
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
  } = props;
  const ids = outerItems.map(x => x.key)

  // Maintain an internal list of items for proper animation
  const [items, setItems] = React.useState(outerItems);
  const [activeId, setActiveId] = React.useState(null);

  React.useEffect(() => {
    setItems(outerItems)
  }, [outerItems])

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint }),
  );

  const handleDragStart = (e) => {
    setActiveId(e.active.id);
  };

  const handleDragEnd = (e) => {
    const { active, over } = e;
    setActiveId(null);

    if (active.id !== over.id) {
      setItems((items) => {
        const oldIndex = ids.indexOf(active.id);
        const newIndex = ids.indexOf(over.id);
        const newItems = arrayMove(items, oldIndex, newIndex);
        onUpdateItemsOrder(items[oldIndex].key, items[newIndex].key, oldIndex, newIndex);
        return newItems;
      });
    }
  };

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      onDragEnd={handleDragEnd}
      onDragStart={handleDragStart}
    >
      <SortableContext strategy={verticalListSortingStrategy} items={items}>
        {items.map(({ ...props }) => <Item
          {...props}
        ></Item>)}
      </SortableContext>

      <Portal>
        <DragOverlay>
          {activeId ? <ItemDragOverlay key={activeId} id={activeId} /> : null}
        </DragOverlay>
      </Portal>
    </DndContext>
  );
};
