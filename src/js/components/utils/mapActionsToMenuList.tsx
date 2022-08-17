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


type ActionsListMenuItem = MenuItemProps & {
  type: "action";
  title: React.ReactNode;
  tooltip?: React.ReactNode;
  tooltipProps?: TooltipProps;
  fn: (any) => void;
}

type ActionsListDivider = {
  type: "divider";
}

type ActionsListGroup = MenuGroupProps & {
  type: 'group';
  title?: React.ReactNode;
  items: Exclude<ActionsListItem, ActionsListGroup>[];
}

export type ActionsListItem = ActionsListMenuItem | ActionsListDivider | ActionsListGroup;

interface mapActionsToMenuListProps {
  target: any;
  menuItems?: ActionsListItem[];
}

/**
 * mapActionsToMenuList
 * @param target The target object to pass into the functions
 * @param actions The actions to map to menu items 
 * @returns an array of items which can be rendered in a MenuList
 */
export const mapActionsToMenuList = ({ target, menuItems }: mapActionsToMenuListProps): React.ReactChild[] => {
  if (!target) console.error("mapActionsToMenuList: target is undefined");
  if (!menuItems) return null;

  return menuItems.map((menuItem, index) => {
    const { type, ...itemProps } = menuItem;

    if (type === "divider") {
      return <MenuDivider key={"divider-" + index} />;

    } else if (type === "group") {
      const { items, ...groupProps } = itemProps;
      return (
        <MenuGroup {...groupProps}>
          {mapActionsToMenuList({ target, menuItems: items })}
        </MenuGroup>
      );

    } else if (type === "action") {
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
    } else {
      return null;
    }
  });
};