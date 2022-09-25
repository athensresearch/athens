import React, { useState } from "react";
import {
  DndContext,
  DragOverlay,
  closestCenter,
  // KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  closestCorners
} from "@dnd-kit/core";
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy
} from "@dnd-kit/sortable";

// create this tiny wrapper around @dnd-kit/core's DnDContext because needs to be a function components
// and cljs components do not use function components normally without some additional ceremony
export const DragAndDropContext = (props) => {
  const {children, ...rest} = props
  return (
    <DndContext
      sensors={useSensors(useSensor(PointerSensor))}
      {...rest}
    >
      {children}
    </DndContext>
  );
};
