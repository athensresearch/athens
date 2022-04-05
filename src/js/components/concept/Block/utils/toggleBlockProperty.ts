
export const toggleBlockProperty = (uid, property, setBlockState) => {
  setBlockState(prevState => {
    return ({
      ...prevState,
      blocks: {
        ...prevState.blocks,
        [uid]: {
          ...prevState.blocks[uid],
          [property]: !prevState.blocks[uid][property]
        }
      }
    })
  })
}