import {
  Tooltip,
  MenuItem,
  MenuDivider,
  MenuGroup,
  MenuItemProps,
  MenuGroupProps,
  TooltipProps,
} from "@chakra-ui/react";
import React from "react";

type ActionsListItemType = "action" | "group" | "divider";

type BaseActionsListItem = {
  type: ActionsListItemType;
  title: React.ReactNode;
}

type ActionsListMenuItem = BaseActionsListItem & MenuItemProps & {
  type: "action";
  tooltip?: React.ReactNode;
  tooltipProps?: TooltipProps,
  fn: (any) => void;
}

type ActionsListDivider = BaseActionsListItem & {
  type: "divider";
}

type ActionsListGroup = BaseActionsListItem & MenuGroupProps & {
  type: 'group';
  items: Exclude<ActionsListItem, ActionsListGroup>[];
}

export type ActionsListItem = ActionsListMenuItem | ActionsListDivider | ActionsListGroup;

interface mapActionsToMenuListProps {
  target: any;
  menuItems: ActionsListItem[];
}

/**
 * mapActionsToMenuList
 * @param target The target object to pass into the functions
 * @param actions The actions to map to menu items 
 * @returns an array of items which can be rendered in a MenuList
 */
export const mapActionsToMenuList = ({ target, menuItems }: mapActionsToMenuListProps): React.ReactChild[] => {
  if (!menuItems) return null;

  return menuItems.map((menuItem, index) => {
    const { type, ...itemProps } = menuItem;

    switch (type) {

      case "divider":
        return <MenuDivider key={"divider-" + index} />;

      case "group":
        const { items, ...groupProps } = itemProps;
        return (
          <MenuGroup {...groupProps}>
            {mapActionsToMenuList({ target, menuItems: items })}
          </MenuGroup>
        );

      case "action":
      default:
        const { title, tooltip, tooltipProps, ...menuItemProps } = itemProps;
        const niceProps = menuItemProps as MenuItemProps;

        if (tooltip) {
          return (
            <Tooltip
              {...tooltipProps}
              label={tooltip}
              hasArrow
              placement="right"
            >
              <MenuItem {...niceProps}>{title}</MenuItem>
            </Tooltip >
          );
        } else {
          return <MenuItem {...niceProps}>{title}</MenuItem>
        }
    }
  });
};
