import { ArchiveIcon, ArrowRightIcon, CheckmarkCircleFillIcon } from "@/Icons/Icons";
import * as React from "react";
import { groupBy } from "./groupBy";

export const useGrouping = (items, defaultGrouping) => {
  const [grouping, setGrouping] = React.useState<string | null>(defaultGrouping);

  const sortedItems: any[] = items.sort((a, b) => {
    if (a[grouping] === b[grouping]) return a.id - b.id;
    return a[grouping] < b[grouping] ? -1 : 1
  });
  const groupedItems: any[] = groupBy(sortedItems, grouping);

  return {
    grouping,
    setGrouping,
    groupedItems
  }
}

export const useFilters = (items: any[], defaultFilters, availableFilters) => {
  const [filterIds, setFilterIds] = React.useState<any[]>(defaultFilters);

  const hasMeaningfulFilters: boolean = filterIds !== defaultFilters;
  const resetFilters = () => setFilterIds(defaultFilters);
  const filters: any[] = filterIds.map(id => availableFilters.find(f => f.id === id));
  const filteredItems: any[] = items.filter(item => filters.every(filter => filter.fn(item)));

  return {
    hasMeaningfulFilters,
    filterIds,
    setFilterIds,
    filteredItems,
    resetFilters,
  }
}

export const useList = (items: any[]) => {
  const [selectedItemId, setSelectedItemId] = React.useState<string | null>(null);
  const selectedItemRef: React.MutableRefObject<HTMLElement> = React.useRef<null | HTMLElement>();
  const selectedItem: object = items.find(item => item.id === selectedItemId);
  const deselectItem: () => void = () => setSelectedItemId(null);
  const selectItem = (id: string, ref: React.MutableRefObject<HTMLElement>) => {
    setSelectedItemId(id);
    if (ref.current) selectedItemRef.current = ref.current;
  };

  return {
    selectedItemId,
    selectedItemRef,
    selectedItem,
    selectItem,
    deselectItem,
  }
}

export const useNotifications = (items: any[], setItems, availableFilters: any[], defaultFilters: any[]) => {

  const {
    selectedItemId,
    selectedItemRef,
    selectedItem,
    selectItem,
    deselectItem,
  } = useList(items);

  // const {
  //   hasMeaningfulFilters,
  //   filterIds,
  //   setFilterIds,
  //   filteredItems,
  //   resetFilters,
  // } = useFilters(items, defaultFilters, availableFilters);

  // const {
  //   grouping,
  //   setGrouping,
  //   groupedItems: groupedFilteredSortedItems,
  // } = useGrouping(filteredItems, "type");

  const updateItemProperty = (id, property, value) => {
    const editedIndex = items.findIndex(item => item.id === id);
    const editedItem = { ...items[editedIndex], [property]: value };
    const editedItems = [...items.slice(0, editedIndex), editedItem, ...items.slice(editedIndex + 1)];
    setItems(editedItems);
  };
  const markAsRead = (id) => updateItemProperty(id, "isRead", true);
  const markAsUnread = (id) => updateItemProperty(id, "isRead", false);
  const markAsArchived = (id) => updateItemProperty(id, "isArchived", true);
  const markAsUnarchived = (id) => updateItemProperty(id, "isArchived", false);
  const openItem = (id) => {
    const openedItem = items.find(item => item.id === id);
    alert(`Opened ${openedItem.object?.name || openedItem.object?.string}`);
  };
  const getActionsForNotification = (notification) => {
    const actions = [];
    if (notification.isRead) {
      actions.push({
        label: "Mark as unread",
        fn: () => markAsUnread(notification.id),
        icon: <CheckmarkCircleFillIcon />
      });
    } else {
      actions.push({
        label: "Mark as read",
        fn: () => markAsRead(notification.id),
        icon: <CheckmarkCircleFillIcon />
      });
    }
    if (notification.isArchived) {
      actions.push({
        label: "Unarchive",
        fn: () => markAsUnarchived(notification.id),
        icon: <ArchiveIcon />
      });
    } else {
      actions.push({
        label: "Archive",
        fn: () => markAsArchived(notification.id),
        icon: <ArchiveIcon />
      });
    }
    actions.push({
      label: "Open",
      fn: () => openItem(notification.id),
      icon: <ArrowRightIcon />
    });
    return actions;
  }

  return {
    getActionsForNotification,
    markAsRead,
    markAsArchived,
    markAsUnread,
    markAsUnarchived,
    openItem,
    selectedItemId,
    selectedItemRef,
    selectedItem,
    selectItem,
    deselectItem,
    // grouping,
    // setGrouping,
    // hasMeaningfulFilters,
    // filterIds,
    // setFilterIds,
    // filteredItems,
    // groupedFilteredSortedItems,
    // resetFilters,
  };
};
