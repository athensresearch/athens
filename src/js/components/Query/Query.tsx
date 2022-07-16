import * as React from "react";
import {
    Button,
  ChakraProvider,
  Heading,
  MenuItem,
  Menu,
  MenuButton,
  MenuItemOption,
  MenuList,
  MenuOptionGroup
} from "@chakra-ui/react";
import { ChevronDownIcon } from '@/Icons/Icons';

export const Controls = (props) => {
    const { isCheckedFn, properties, hiddenProperties, onChange, menuOptionGroupValue } = props
  return (
    <Menu closeOnSelect={false}>
      <MenuButton><Heading size="md" as={Button} rightIcon={<ChevronDownIcon/>}>Hidden Properties</Heading></MenuButton>
      <MenuList>
        <MenuOptionGroup type="checkbox" onChange={(e) => console.log(e) } value={menuOptionGroupValue}>
          {properties.map((property) => {
            const isActive = hiddenProperties[property]
            return <MenuItemOption color={isActive ? "gray" : "black"} value={property} onClick={(e) => onChange(e.target.innerHTML)}>{property}</MenuItemOption>
          })}
        </MenuOptionGroup>
      </MenuList>
    </Menu>
  );
}