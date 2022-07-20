
/**
 * formatList
 * @param items 
 * @returns A readable list with commas and "and" separators as needed. If no items provided, returns false.
 */
export const formatList = (items: string[]): string | false => {
  if (!items.length) {
    return false;
  } else if (items.length === 1) {
    return items[0];
  } else if (items.length === 2) {
    return `${items[0]} and ${items[1]}`;
  } else {
    const lastName = items[items.length - 1];
    const restitems = items.slice(0, items.length - 1);
    return restitems.join(", ") + ", and " + lastName;
  }
};