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
  MenuOptionGroup,
  VStack,
  Text
} from "@chakra-ui/react";
import { ChevronDownIcon, ViewIcon } from '@/Icons/Icons';

export const Controls = (props) => {
  const { isCheckedFn, properties, hiddenProperties, onChange, menuOptionGroupValue } = props
  return (
    <Menu closeOnSelect={false} size="sm">
      <Button as={MenuButton} size="sm" rightIcon={<ChevronDownIcon />}>Hidden Properties</Button>
      <MenuList>
        <MenuOptionGroup type="checkbox" onChange={(e) => console.log(e)} value={menuOptionGroupValue}>
          {properties.map((property) => {
            const isActive = hiddenProperties[property]
            return <MenuItemOption
              icon={<ViewIcon />}
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
    <Menu closeOnSelect={false} size="sm">
      <Button
        as={MenuButton}
        height="auto"
        rightIcon={<ChevronDownIcon gridArea="icon" />}
        display="grid"
        gridTemplateAreas="'main icon'"
        py={1}
        gap={2}
        sx={{
          "> span, chakra-button__icon": {
            display: "contents"
          }
        }}
      >
        <VStack
          align="stretch"
          spacing={0}
          textAlign="start"
          gridArea="main"
        >
          <Text>{heading}</Text>
          <Text fontWeight="normal" color="foreground.secondary">{value}</Text>
        </VStack>
      </Button>
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