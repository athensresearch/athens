import * as React from "react";

export const groupBy = (objectArray, property) => {
  return objectArray.reduce(function (acc, obj) {
    let key = obj[property]
    if (!acc[key]) {
      acc[key] = []
    }
    acc[key].push(obj)
    return acc
  }, {})
};

export const useNotifications = (items, setItems, availableFilters, defaultFilters) => {
  const [selectedItemId, setSelectedItemId] = React.useState(null);
  const [filterIds, setFilterIds] = React.useState(defaultFilters);
  const filters = filterIds.map(id => availableFilters.find(f => f.id === id));
  const filteredItems = items.filter(item => filters.every(filter => filter.fn(item)));
  const groupedFilteredItems = groupBy(filteredItems.sort(), "type");
  const selectedItemRef = React.useRef<null | HTMLElement>();
  const selectedItem = items.find(item => item.id === selectedItemId);
  const hasMeaningfulFilters = filterIds !== defaultFilters;
  const resetFilters = () => setFilterIds(defaultFilters);
  const updateItemProperty = (id, property, value) => {
    const editedIndex = items.findIndex(item => item.id === id);
    const editedItem = { ...items[editedIndex], [property]: value };
    const editedItems = [...items.slice(0, editedIndex), editedItem, ...items.slice(editedIndex + 1)];
    setItems(editedItems);
  };
  const selectItem = (id, ref) => {
    setSelectedItemId(id);
    selectedItemRef.current = ref.current;
  };
  const deselectItem = () => setSelectedItemId(null);
  const markAsRead = (id) => updateItemProperty(id, "isRead", true);
  const markAsUnread = (id) => updateItemProperty(id, "isRead", false);
  const markAsArchived = (id) => updateItemProperty(id, "isArchived", false);
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
      });
    } else {
      actions.push({
        label: "Mark as read",
        fn: () => markAsRead(notification.id),
      });
    }
    if (notification.isArchived) {
      actions.push({
        label: "Unarchive",
        fn: () => markAsUnarchived(notification.id),
      });
    } else {
      actions.push({
        label: "Archive",
        fn: () => markAsArchived(notification.id),
      });
    }
    actions.push({
      label: "Open",
      fn: () => openItem(notification.id),
    });
    return actions;
  }

  return {
    filterIds,
    setFilterIds,
    selectedItemId,
    selectedItemRef,
    selectedItem,
    hasMeaningfulFilters,
    filteredItems,
    groupedFilteredItems,
    getActionsForNotification,
    resetFilters,
    selectItem,
    deselectItem,
    markAsRead,
    markAsArchived,
    markAsUnread,
    markAsUnarchived,
    openItem,
  };
};
