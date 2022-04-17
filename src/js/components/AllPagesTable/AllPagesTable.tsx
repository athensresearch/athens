import React from 'react';
import { FixedSizeList as List } from 'react-window';
import { Box, Table, Thead, Tfoot, Tbody, Th, Td, Tr } from '@chakra-ui/react';


const Row = ({ index, data, style }) => {
  console.log('rendered item')
  return (
    <Tr style={style}><Td>{data[index].title}</Td></Tr>
  )
};

const getHeight = (el) => {
  if (el) {
    return el.offsetHeight;
  } else {
    return 500;
  }
}

export const AllPagesTable = ({ sortedPages }) => {

  const containerRef = React.useRef();
  const [containerHeight, setContainerHeight] = React.useState(500);

  React.useLayoutEffect(() => {
    const updateSize = () => {
      setContainerHeight(getHeight(containerRef.current))
    };
    window.addEventListener("resize", updateSize);
    updateSize();
    return () => window.removeEventListener("resize", updateSize);
  })

  return <Box
    ref={containerRef}
    px={4}
    width="100%"
    maxWidth="75rem"
    margin="calc(var(--app-header-height) + 2rem) auto 5rem"
    height="calc(100% - var(--app-header-height) - 7rem)"
  >
    <Table border="1px solid white">
      <Tbody border="1px solid red">
        <List
          height={containerHeight}
          itemCount={sortedPages.length}
          itemSize={35}
          itemData={sortedPages}
          width={300}
        >
          {Row}
        </List>
      </Tbody>
    </Table>
  </Box>
}
