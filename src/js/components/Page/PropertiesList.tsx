import React from 'react';
import { Box, Image, List, ListItem, VStack, FormControl, Flex, FormLabel, Text, Switch, Divider, Input, InputGroup, IconButton, InputRightElement, HStack, FormHelperText } from '@chakra-ui/react';
import { XmarkIcon } from '@/Icons/Icons';

const LabeledText = ({ label, value }) => {
  return <HStack align="flex-start">
    <Text color="foreground.secondary">{label}</Text><Text>{value}</Text></HStack>;
}

const LabeledList = ({ label, values }) => {
  return <HStack align="flex-start">
    <Text color="foreground.secondary">{label}</Text>
    <List>{values.map(v => <ListItem>{v}</ListItem>)}</List>
  </HStack>;
}

const EditableBoolean = ({ label, value, help, onChange }) => {
  return <FormControl as="label">
    <Flex align="center" gap={-3}>
      <FormLabel mb={0} flex={1} as="div">
        {label}
      </FormLabel>
      <Switch isChecked={value} onChange={onChange} />
    </Flex>
    {help && <FormHelperText>{help}</FormHelperText>}
  </FormControl>
}

const EditableText = ({ label, value, help, onChange }) => {
  return <FormControl as="label">
    <Flex align="center" gap={-3}>
      <FormLabel mb={0} flex={1} as="div">
        {label}
      </FormLabel>
      <Switch isChecked={value} onChange={onChange} />
    </Flex>
    {help && <FormHelperText>{help}</FormHelperText>}
  </FormControl>
}

const isValidUrl = (url) => {
  try {
    new URL(url);
  } catch (error) {
    return false;
  }
  return true;
};

interface EditableImageUrlProps {
  label: string;
  value: string;
  help?: string;
  placeholder?: string;
  onChange: (value: string) => void;
  shouldShowPreview?: boolean;
}

const EditableImageUrl = (props: EditableImageUrlProps) => {
  const { label, value, help, onChange, placeholder, shouldShowPreview = true } = props;

  const inputRef = React.useRef<null | HTMLInputElement>(null);
  const [isInputValid, setIsInputValid] = React.useState<boolean>(true);

  const handleChange = (e) => {
    let result = e.target.value;

    if (result === "" || isValidUrl(result)) {
      onChange(result);
      if (inputRef.current) {
        inputRef.current.value = result;
      }
    } else {
      setIsInputValid(false);
    }
  };

  const handleClear = () => {
    const emptyValue = "";
    inputRef.current.value = emptyValue;
    onChange(emptyValue);
    setIsInputValid(true);
  };

  return (
    <FormControl isInvalid={!isInputValid}>
      <FormLabel>{label}</FormLabel>
      <InputGroup size="sm">
        <Input
          type="url"
          ref={inputRef}
          onBlur={handleChange}
          defaultValue={value}
          placeholder={placeholder}
        />
        {(value || inputRef?.current?.value) && (
          <InputRightElement>
            <IconButton
              onClick={handleClear}
              aria-label="Clear"
              size="xs"
              icon={<XmarkIcon />}
            />
          </InputRightElement>
        )}
      </InputGroup>
      {help && <FormHelperText>{help}</FormHelperText>}
      {shouldShowPreview && value && (
        <Box as="output" mt={2} display="flex">
          <Image src={value} />
        </Box>
      )}
    </FormControl>
  );
};

export const PropertiesList = (props) => {
  const {
    headerImageUrl,
    onChangeHeaderImageUrl,
    headerImageEnabled,
  } = props;

  return (
    <VStack align="stretch" p={2} spacing={6} divider={<Divider />}>
      {/* <EditableBoolean
        help="Show this page in the left sidebar"
        label="Show in sidebar"
        value={hasShortcut}
        onChange={onChangeHasShortcut}
      /> */}

      {headerImageEnabled && (<>
        <EditableImageUrl
          label="Header image url"
          value={headerImageUrl}
          placeholder="https://"
          help="An image to use as a header for this page"
          onChange={onChangeHeaderImageUrl}
        />
      </>)}

      {/* <VStack align="stretch">
        <LabeledText label="Creator" value="Jeff Tang" />
        <LabeledList label="Contributors" values={["Jeff Tang", "Alex Iwaniuk"]} />
        <LabeledText label="Created" value="Jan 1, 1970" />
        <LabeledText label="Last updated" value="June 16, 2022" />
      </VStack> */}
    </VStack >
  );
};
