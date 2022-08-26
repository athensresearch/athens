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
import { ChevronDownIcon, ViewIcon } from '@/Icons/Icons';

export const Controls = (props) => {
    const { isCheckedFn, properties, hiddenProperties, onChange, menuOptionGroupValue } = props
  return (
    <Menu closeOnSelect={false}>
      <MenuButton><Heading size="sm" as={Button} rightIcon={<ChevronDownIcon/>}>Hidden Properties</Heading></MenuButton>
      <MenuList>
        <MenuOptionGroup type="checkbox" onChange={(e) => console.log(e) } value={menuOptionGroupValue}>
          {properties.map((property) => {
            const isActive = hiddenProperties[property]
            return <MenuItemOption
                icon={<ViewIcon/>}
                value={property}
                onClick={(e) => onChange(e.target.innerHTML)}>{property}
            </MenuItemOption>
          })}
        </MenuOptionGroup>
      </MenuList>
    </Menu>
  );
}


export const QueryRadioMenu = (props) => {
  const { heading, options, onChange, value } = props
  return (
    <Menu closeOnSelect={false}>
      <MenuButton><Heading size="sm" as={Button} rightIcon={<ChevronDownIcon/>}>{heading}</Heading></MenuButton>
      <MenuList>
        <MenuOptionGroup type="radio" onChange={onChange} value={value}>
            {options.map(x =>
                <MenuItemOption value={x}>
                    {x}
                </MenuItemOption>
            )}
        </MenuOptionGroup>
      </MenuList>
    </Menu>
  );
}

export const AuthorFilter = (props) => {
    return 1
}

export const ThisPageFilter = (props) => {
    return 1
}