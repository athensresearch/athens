import {
  MenuItem,
  MenuList
} from "@chakra-ui/react";

export const mapActionsToMenuList = (actions) => {
  return <MenuList>
    {actions.map(action => {
      const { label, fn, icon, command } = action;
      return <MenuItem
        key={label}
        onClick={fn}
        icon={icon}
        command={command}
      >{action.label}</MenuItem>
    })}
  </MenuList>;
};
